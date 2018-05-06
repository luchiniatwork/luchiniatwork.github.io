{:title  "Language comparison"
 :layout :post
 :date   "2018-03-21"
 :tags   ["technology" "python" "clojure" "ocaml"]}
 
Here’s the problem description: write an algorithm that sums one million entries from a sequence where each entry is the sin of the previous entry. The sequence starts with `0.3` and, for verification, the result is `3443.960292891887`.

Kind of a very simple problem but I was impressed with how slow and inefficient I was able to make it

## Naive Python implementation

```python

import numpy as np
from math import sin

def doit():
    results = np.array(range(1000000),dtype=np.float64)
    results[0] = 0.3
    for i in range(1,1000000):
        results[i] = sin(results[i-1])
    return sum(results[::-1])
```

## Less naive Python implementation

```python
import math

def doit2():
    r = 0.3
    n = 0.3
    i = 0
    sin = math.sin
    while i < 1000000 - 1:
        n = sin(n)
        r += n
        i += 1
    return r
```

## Same as above but in NodeJS

```javascript
function doit2() {
  let r = 0.3;
  let n = 0.3;
  const sin = Math.sin;
  for (let i = 0; i < 1000000 - 1; i += 1) {
    n = sin(n);
    r += n;
  }
  return r;
}
```

## Recursion limit on Python causes challenges

I could not go over `sys.setrecursionlimit(10000)` and still compute it. Nevertheless, for 10K values I would say it is not bad.

```python
def doit_recursively(n,v):
    if n==0:
        return v
    else:
        partial = sin(v)
        return partial + doit_recursively(n-1, partial)

%time x = doit_recursively(10000,0.3)
```

## Simple reducer on Clojure

```clojure
(time (reduce + (take 1e6 (iterate (fn [^double x] (Math/sin x)) 0.3))))
```

## Loop recursion on Clojure

```clojure
(time (loop [i 1 n 0.3 sum 0] (if (>= i 1e6) (+ sum n) (recur (inc i) (Math/sin n) (+ sum n)))))
```

## Transducer on Clojure

```clojure
(time (transduce (take 1e6) + (iterate #(Math/sin %) 0.3)))
```

## Functional in Python

```python
from math import sin

def add(x, y):
    return x + y

def iterate(func, seed):
    v = seed
    while True:
        v = func(v)
        yield v

def reducer(func, times, generator, seed):
    reduced = 0
    for value in generator:
        if times:
            times -= 1
            reduced += value
        else:
            return reduced
```

## Tail recursion OCAML

```ocaml
let rec sin_sum ?(sum = 0.) value count =
  if count = 0 then
    sum
  else
    let sin = sin value in
    sin_sum ~sum:(sum +. value) sin (count - 1)

let () =
  print_string "Total: ";
  print_float (sin_sum 0.3 1000000);
  print_newline ()
```

## Tail recursion Clojure

```clojure
  (defn sin-sum
    ([value count]
     (sin-sum value count 0.0))
    ([value count sum]
     (if (= count 0)
       sum
       (let [sin (Math/sin value)]
         (recur sin (dec count) (+ value sum))))))

  (time (sin-sum 0.3 (int 1e6)))
```
