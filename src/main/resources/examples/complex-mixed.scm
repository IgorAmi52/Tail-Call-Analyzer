(define (f x y z)
  (g x)
  (if x
      (if y
          (f (f x) (f y) z)
          (if z
              (f 1)
              (g (f 2) (f 3))))
      (f (if x
              (f 4)
              5)
         y
         (if z 1 2))))
