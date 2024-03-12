package com.fern.seq;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static com.fern.util.Util.*;

public class List extends ABaseSeq {

    private static final Object AVAILABLE_SLOT = new Object() {
        @Override
        public String toString() {
            return "AVAILABLE_SLOT";
        }
    };
    private static final int RESIZE_EXTRA_SLOTS = 15;

    public static List neu(Object... elements) {
        if (elements == null) {
            throw new NullPointerException();
        }
        return new List(elements);
    }

    private final int start;
    private final int end;
    private final int size;
    private final Object[] elements;
    private final int hashCode;
    private final AtomicReference<Object[]> quickToArray;
    private final AtomicReference<String> quickStr; // acts as cons lock
    private final AtomicReference<ISeq> quickRest;  // acts as cone lock

    public List(Object... elements) {
        this(0, safeLen(elements), elements);
    }

    public List(int start, int end, Object... elements) {
        if (elements == null) {
            throw new NullPointerException();
        }
        if (start < 0 || end < start || end > elements.length) {
            throw new IllegalArgumentException(str(
                    "bad range [%d, %d], elements.length is %d",
                    start, end, elements.length));
        }
        this.hashCode = hashCode(start, end, elements);
        this.size = end - start;
        this.start = start;
        this.end = end;
        this.elements = elements;
        this.quickStr = new AtomicReference<>();
        this.quickRest = new AtomicReference<>();
        this.quickToArray = new AtomicReference<>();
    }

    private static int hashCode(int start, int end, Object[] array) {
        int result = 11;
        for (int i = start; i < end; i++) {
            Object el = array[i];
            result = 31 * result + (el == null ? 0 : el.hashCode());
        }
        return result;
    }

    /**
     * Takes zero or one parameter.
     * <p>
     * No parameters: if the list contains null it will return its position, otherwise null.
     * <br/>
     * One parameter: will search for the parameter returning its position, otherwise null.
     *
     * @param args,
     * @return null or the position of the first arg in the list
     */
    @Override
    public Object invoke(Object... args) {
        if (args != null && (args.length == 0 || args.length > 1)) {
            throw new IllegalArgumentException("only one arg is allowed, to return its position if found, or null");
        }
        Object target = args != null ? args[0] : null;
        for (int i = start; i < end; i++) {
            Object o = elements[i];
            if ((o == null && target == null) || (o != null && target != null && o.equals(target))) {
                return i;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ISeq that) {
            if (size != that.size()) {
                return false;
            }
            Iterator<Object> thatIterator = that.iterator();
            for (int i = start; i < end && thatIterator.hasNext(); i++) {
                Object e1 = elements[i];
                Object e2 = thatIterator.next();
                if (false == (e1 == null ? e2 == null : e1.equals(e2))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String str = quickStr.get();
        if (str != null) {
            return str;
        }
        StringBuilder sb = THR_SB.get();
        sb.append("[");
        for (int i = start; i < end; i++) {
            sb.append(elements[i]).append(TO_STR_SEP);
        }
        if (size > 0) {
            sb.setLength(sb.length() - TO_STR_SEP.length());
        }
        sb.append("]");
        quickStr.compareAndSet(null, sb.toString());
        return quickStr.get();
    }

    @Override
    public Object first() {
        return size == 0 ? null : elements[start];
    }

    @Override
    public Object last() {
        return size == 0 ? null : elements[end - 1];
    }

    @Override
    public Object nth(int n) {
        int offset = start + n;
        if (offset >= start && offset < end) {
            return elements[offset];
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public ISeq rest() {
        ISeq rest = quickRest.get();
        if (rest != null) {
            return rest;
        }
        switch (size) {
            case 0:
            case 1:
                quickRest.compareAndSet(null, NIL);
                break;
            default:
                quickRest.compareAndSet(null, new List(start + 1, end, elements));
        }
        return quickRest.get();
    }

    @Override
    public ISeq cons(Object e) {
        boolean filledAvailableSlot = false;
        synchronized (quickStr) {
            if (start > 0 && elements[start - 1] == AVAILABLE_SLOT) {
                elements[start - 1] = e;
                filledAvailableSlot = true;
            }
        }
        ISeq array;
        if (filledAvailableSlot) {
            array = new List(start - 1, end, elements);
        } else {
            Object[] els = new Object[RESIZE_EXTRA_SLOTS + size];
            int headOfNewList = RESIZE_EXTRA_SLOTS;
            Arrays.fill(els, 0, headOfNewList, AVAILABLE_SLOT);
            els[headOfNewList - 1] = e;
            System.arraycopy(elements, start, els, headOfNewList, size);
            array = new List(headOfNewList - 1, els.length, els);
        }
        return array;
    }

    @Override
    public ISeq cone(Object e) {
        boolean filledAvailableSlot = false;
        synchronized (quickRest) {
            if (end < elements.length && elements[end] == AVAILABLE_SLOT) {
                elements[end] = e;
                filledAvailableSlot = true;
            }
        }
        ISeq array;
        if (filledAvailableSlot) {
            array = new List(start, end + 1, elements);
        } else {
            Object[] els = new Object[size + RESIZE_EXTRA_SLOTS];
            System.arraycopy(elements, start, els, 0, size);
            els[end] = e;
            Arrays.fill(els, end + 1, end + RESIZE_EXTRA_SLOTS, AVAILABLE_SLOT);
            array = new List(start, end + 1, els);
        }
        return array;
    }

    @Override
    public Object[] toArray() {
        Object[] array = quickToArray.get();
        if (array != null) {
            return array;
        }
        array = new Object[size];
        System.arraycopy(elements, start, array, 0, size);
        quickToArray.compareAndSet(null, array);
        return quickToArray.get();
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            private int idx = start;

            @Override
            public boolean hasNext() {
                return idx < end;
            }

            @Override
            public Object next() {
                if (hasNext()) {
                    return elements[idx++];
                }
                throw new IndexOutOfBoundsException();
            }
        };
    }

    @Override
    public ISeq sorted() {
        return sorted(DEFAULT_COMPARATOR);
    }

    @Override
    public ISeq sorted(Comparator<Object> comparator) {
        Object[] els = new Object[size];
        System.arraycopy(elements, start, els, 0, size);
        Arrays.sort(els, comparator);
        return new List(els);
    }
}