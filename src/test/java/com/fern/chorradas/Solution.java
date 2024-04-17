package com.fern.chorradas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Solution {

    public static class MinHeap {
        private int[] arr;
        private int idx;

        public MinHeap() {
            arr = new int[16];
        }

        public int peek() {
            if (idx == 0) {
                throw new IllegalStateException("empty");
            }
            return arr[0];
        }

        public void add(int e) {
            if (idx >= arr.length) {
                int[] tmp = new int[arr.length << 1]; // grow
                System.arraycopy(arr, 0, tmp, 0, arr.length);
                arr = tmp;
            }
            arr[idx++] = e; // add at the end
            siftUp();
        }

        public int size() {
            return idx;
        }

        public int remove() {
            if (idx == 0) {
                throw new IllegalStateException("empty");
            }
            int top = arr[0]; // take top of the heap
            arr[0] = arr[--idx]; // place the bottom-most at the top
            siftDown(0);
            return top;
        }

        public void remove(int e) {
            if (idx == 0) {
                throw new IllegalStateException("empty");
            }

            int i = 0;
            for (; i < idx; i++) {
                if (arr[i] == e) {
                    break;
                }
            }
            if (i < idx) {
                arr[i] = arr[--idx];
                siftDown(i);
            }
        }

        private void siftUp() {
            // sift up to restore heap property
            int k = idx - 1;
            while (k > 0) {
                int p = (k - 1) / 2; // parent
                if (arr[p] > arr[k]) {
                    swap(k, p);
                }
                k = p;
            }
        }

        private void siftDown(int start) {
            // sift down to maintain heap property
            int p = start;
            while (p < idx) {
                int l = 2 * p + 1; // left
                int r = 2 * p + 2; // right
                if (r < idx) { // there is a right child, which means there is also a left child
                    if (arr[l] < arr[p] || arr[r] < arr[p]) { // the parent is bigger, so swap by smallest child
                        if (arr[l] < arr[r]) { // use the left one
                            swap(p, l);
                            p = l;
                        } else { // use the right one
                            swap(p, r);
                            p = r;
                        }
                    } else {
                        p = r;
                    }
                } else if (l < idx) { // there is a left child, but no right child
                    if (arr[p] > arr[l]) {
                        swap(p, l);
                    }
                    p = l;
                } else {
                    break;
                }
            }
        }

        private void swap(int i, int j) {
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }


    public static void main(String[] args) throws Exception {
        MinHeap h = new MinHeap();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            int cases = Integer.parseInt(in.readLine());
            for (int i = 0, lineNo = 0; i < cases; i++, lineNo++) {
                String[] line = in.readLine().split("\s+");
                switch (line[0]) {
                    case "1":
                        h.add(Integer.parseInt(line[1]));
                        break;
                    case "2":
                        h.remove(Integer.parseInt(line[1]));
                        break;
                    case "3":
                        System.out.println(h.peek());
                        break;
                }
            }
        }
    }
}
