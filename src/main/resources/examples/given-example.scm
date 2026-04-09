(define (f x)
  (if x
      (f 1 (f 4))
      (if x
          (f 1)
          4)))
