#!/bin/python
import random
l = [random.random() for _ in range(5)]
w = [r * 1.0 / sum(l) for r in l]
print w[0], w[1], w[2], w[3], w[4]
