package unsatj.unsatj;

import java.util.LinkedHashSet;

public class Csp {

    private long[][] constraints;
    private int[][] clauses;
    private int maxBool;
    
    private int if1 = 0;
    private int if2 = 0;
    
    public Csp(int[][] clauses) {
        this.clauses = clauses;
        System.out.println("Count clauses: " + clauses.length);
        maxBool = getMaxBool();
        boolAssign = new boolean[maxBool+1];
        boolIsAssigned = new boolean[maxBool+1];
        constraints = new long[clauses.length][clauses.length];
        int calls = 0;
        int satisfying = 0;
        for (int c=0; c<clauses.length; c++) {
            for (int c2=c+1; c2<clauses.length; c2++) {
                long constraint = 0L;
                for (int assignments=0; assignments<64; assignments++) {
                    calls++;
                    if (isSatisfying(c, c2, assignments)) {
                        constraint |= 1L << assignments;
                        satisfying++;
                    }
                }
                constraints[c][c2] = constraint;
            }
        }
        //System.out.println(if1 + " " + if2 + "; CALLS: " + calls + "; SATISFYING: " + satisfying);
    }    

    boolean[] boolAssign;
    boolean[] boolIsAssigned;

    private boolean isSatisfying(int c1, int c2, int assignments) {
        for (int i=0; i<boolIsAssigned.length; i++) {
            boolIsAssigned[i] = false;
        }
        boolean satisfied = false;
        for (int b=0; b<3; b++) {
            boolean assignment = (assignments & (1 << b)) != 0;
            int bool = Math.abs(clauses[c1][b]);
            if (!boolIsAssigned[bool] || boolAssign[bool] == assignment) {
                boolAssign[bool] = assignment;
                boolIsAssigned[bool] = true;
                if1++;
            } else {
                return false;
            }
            if ((clauses[c1][b]>0 && assignment) || (clauses[c1][b]<0 && !assignment)) {
                satisfied = true;
            }
        }
        if (! satisfied) {
            return false;
        }
        satisfied = false;
        for (int b=0; b<3; b++) {
            boolean assignment = (assignments & (1 << (b+3))) != 0;
            int bool = Math.abs(clauses[c2][b]);
            if (!boolIsAssigned[bool] || boolAssign[bool] == assignment) {
                boolAssign[bool] = assignment;
                boolIsAssigned[bool] = true;
                if2++;
            } else {
                return false;
            }
            if ((clauses[c2][b]>0 && assignment) || (clauses[c2][b]<0 && !assignment)) {
                satisfied = true;
            }
        }
        if (! satisfied) {
            return false;
        }
        return true;
    }
  
    private int getMaxBool() {
        int maxBool = 0;
        for (int c=0; c<clauses.length; c++) {
            for (int l=0; l<3; l++) {
                maxBool = Math.max(maxBool, Math.abs(clauses[c][l]));
            }
        }
        return maxBool;
    }
       
    public boolean enforcePathConsistency() {
        LinkedHashSet<Pair> pairs = initPairs();
        System.out.println("start");
        while (! pairs.isEmpty()) {
            Pair current = pairs.iterator().next();
            pairs.remove(current);
            for (int k=0; k<clauses.length; k++) {
                if (k==current.clause1 || k==current.clause2) {
                    continue;
                }
                propagate(current.clause1, current.clause2, k, pairs);
            }
        }
        System.out.println("satisfiability: " + (constraints[0][1]!=0L));
        return constraints[0][1] != 0L;
    }
    
    private void propagate(int clause1, int clause2, int k, LinkedHashSet<Pair> pairs) {         
       long out1 = 0L;
       long out2 = 0L;
       int min1 = Math.min(clause1, k);
       int min2 = Math.min(clause2, k);
       int max1 = Math.max(clause1, k);
       int max2 = Math.max(clause2, k);
       for (int n1=0; n1<8; n1++) {
           for (int n2=0; n2<8; n2++) {
               if (0L == (constraints[clause1][clause2] & (1L << (n1+8*n2)))) {
                   continue;
               }
               for (int n3=0; n3<8; n3++) {
                   int shift2 = min2==clause2 ? n2+8*n3 : n3+8*n2;
                   int shift1 = min1==clause1 ? n1+8*n3 : n3+8*n1;
                   boolean ok2 = 0L != (constraints[min2][max2] & (1L << shift2));
                   boolean ok3 = 0L != (constraints[min1][max1] & (1L << shift1));
                   if (ok2) {
                       out1 |= 1L << shift1;
                   }
                   if (ok3) {
                       out2 |= 1L << shift2;  
                   }
               }
           }
       }
       if ((constraints[min1][max1] & out1) != constraints[min1][max1]) {
           constraints[min1][max1] &= out1;
           pairs.add(new Pair(min1, max1, clauses.length));
       }
       if ((constraints[min2][max2] & out2) != constraints[min2][max2]) {
           constraints[min2][max2] &= out2;
           pairs.add(new Pair(min2, max2, clauses.length));
       }
    }
    
    private LinkedHashSet<Pair> initPairs() {
        LinkedHashSet<Pair> pairs = new LinkedHashSet<Pair>();
        for (int c1=0; c1<clauses.length; c1++) {
            for (int c2=c1+1; c2<clauses.length; c2++) {
                if (Long.bitCount(constraints[c1][c2]) < 49) {
                    pairs.add(new Pair(c1, c2, clauses.length));
                }      
                if (Long.bitCount(constraints[c1][c2]) < 6) {
                    System.err.println("ERROR");
                }
            }   
        }
        return pairs;
    }
    
    private static class Pair {
        public final int clause1;
        public final int clause2;
        public final int hashCode;
        
        public Pair(int clause1, int clause2, int clauseLength) {
            this.clause1 = clause1;
            this.clause2 = clause2;
            this.hashCode = clause1 * clauseLength + clause2;
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            return hashCode == other.hashCode;
        }
    }
    
    /*public static void main(String[] args) {
        new Csp(new int[][] {{1,2,3},{1,2,-3},{1,-2,3},{1,-2,-3},
                   {-1,2,3},{-1,2,-3},{-1,-2,3},{-1,-2,-3}}).enforcePathConsistency();
        //new Csp(new int[][] {{1,1,1},{-1,-1,-1}}).enforcePathConsistency();
    }
    */
}
