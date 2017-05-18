#!/bin/python
import random

def random_numbers(n):
	l = [random.random() for _ in range(n-1)]
	l = [0] + sorted(l) + [1]
	l = [b-a for a, b in zip(l, l[1:])]
	return l

w = random_numbers(5)
print w[0], w[1], w[2], w[3], w[4]
