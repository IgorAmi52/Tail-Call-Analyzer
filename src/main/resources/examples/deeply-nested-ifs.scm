(define (f x)
  (if x
      (if x
          (if x
              (f 1)
              (f 2))
          (f 3))
      (f 4)))
