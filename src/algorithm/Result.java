package algorithm;

class Result {
    int numLeftEdge;
    int numRightEdge;
    int numLongest;
    boolean entireRange;
    
    Result(int l, int r, int m, boolean a) {
      numLeftEdge=l; 
      numRightEdge=r; 
      numLongest=m; 
      entireRange=a;
    }
}