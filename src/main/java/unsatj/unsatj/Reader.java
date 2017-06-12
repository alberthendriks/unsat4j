package unsatj.unsatj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    
    int currentFileVars;

    public static void main(String[] args) {
        if (args.length!=1) {
            System.out.println("expected: 1 param, a .cnf file or a dir with .cnf files that are unsatisfiable.");
            System.exit(0);
        }
        File file = new File(args[0]);
        Reader reader = new Reader();
        if (file.isDirectory()) {
            reader.runDir(args[0], false);
        } else if (file.isFile()) {
            new Csp(reader.readFile(args[0])).enforcePathConsistency();
        }       
        
        //new Csp(reader.readFile("C:\\Users\\Guard\\Downloads\\3sat\\edits\\uf20-01.cnf")).enforcePathConsistency();
        //reader.runDir("C:\\Users\\Guard\\Downloads\\3sat", true);
        //reader.runDir("C:\\Users\\Guard\\Downloads\\3sat\\more\\UUF50.218.1000", false);
        //reader.runDir("C:\\Users\\Guard\\Downloads\\3sat\\more\\uf50-218", true);
        //reader.runDir("C:\\Users\\Guard\\Downloads\\3sat\\more\\UUF250.1065.100", false); // problem
        //reader.runDir("C:\\Users\\Guard\\Downloads\\3sat\\more\\UUF75.325.100", false);
        //reader.runDir("C:\\Users\\Guard\\Downloads\\3sat\\more\\UUF100.430.1000", false);
    }
    
    public void runDir(String path, boolean satisfiable) {
        int wrong=0;
        long totalStartTime = System.currentTimeMillis();
        for (String filename: filesInDir(path)) {
            int[][] clauses = readFile(filename);
            long startTime = System.currentTimeMillis();
            Csp csp = new Csp(clauses);
            if (satisfiable != csp.enforcePathConsistency()) {
                System.err.println(filename);
                wrong++;
            }
            System.out.println(filename + ": " + (System.currentTimeMillis() - startTime)/1000 + "s\n");
            System.gc();
        }
        System.out.println("total time: " + (System.currentTimeMillis() - totalStartTime)/1000 + "s");
        System.out.println("wrong: " + wrong);
    }
    
    private List<String> filesInDir(String path) {
        ArrayList<String> filenames = new ArrayList<String>();
        File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            if (! fileEntry.isDirectory() && fileEntry.getName().endsWith(".cnf")) {
                filenames.add(path+"\\"+fileEntry.getName());
            }
        }
        return filenames;
    }
    
    private int[][] readFile(String path) {
        int[][] clauses = null;
        int i=0;
        File file = new File(path);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("c")) {
                    continue;
                }
                if (line.startsWith("p")) {
                    String[] parts = line.split(" ");
                    clauses = new int[Integer.parseInt(parts[4])][3];
                    currentFileVars = Integer.parseInt(parts[2]);
                    continue;
                }
                if (line.startsWith("%")) {
                    break;
                }
                line = line.trim();
                String[] items = line.split(" ");
                for (int l=0; l<3; l++) {
                    clauses[i][l] = Integer.parseInt(items[l]);
                }
                i++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fileReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        checkClauses(clauses);
        return clauses;
    }

    private void checkClauses(int[][] clauses) {
        for (int[] clause: clauses) {
            for (int l: clause) {
                if (l==0) {
                    System.err.println("some literal is 0");
                    System.exit(0);
                }
            }
        }
    }
}
