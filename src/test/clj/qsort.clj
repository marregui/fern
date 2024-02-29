(defn qsort [[pivot & coll]]
  (when pivot
    (concat (qsort (filter #(< % pivot) coll))
            [pivot]
            (qsort (filter #(>= % pivot) coll)))))

(defn genarray [size min max]
  (take size (repeatedly #(+ min (rand-int (+ (- max min) 1))))))

(defn my-time [f]
  (let [start (System/nanoTime)] (do (f) (float (/ (- (System/nanoTime) start) 1000)))))

(defn measure-qsort [sample-size array-size]
  (println 
    (/  
      (reduce + 
        (for [n (range sample-size)]
          (let [a (genarray array-size 0 array-size)]
            (my-time #(qsort a))))) 
      sample-size)))

(measure-qsort 300 2000)
