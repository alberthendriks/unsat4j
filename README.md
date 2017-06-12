A run of this program can prove that a certain cnf instance is unsatisfiable. Where other solvers fail, this program proves unsatisfiability for all instances of 50 variables from http://www.cs.ubc.ca/~hoos/SATLIB/benchm.html (uuf50-218) and also proves unsatisfiability of many other instances from that website. Although the number of code lines seems small, the idea behind this solver is quite intriguing. Each clause of the cnf instance is treated as a variable to a cnf instance with 8 possible values.