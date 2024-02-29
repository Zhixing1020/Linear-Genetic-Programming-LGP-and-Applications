package zhixing.symbolicregression.optimization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ec.gp.GPTree;
import yimei.jss.ruleanalysis.ResultFileReader;
import yimei.jss.ruleanalysis.RuleType;
import zhixing.symbolicregression.util.LispParser;

public class DataProcessing {

	public static void main(String[] args) {
		String path = "D:/exp_data";
        String dataset = "/USCrime/USCrime.txt";
        String outputfile1 = "/USCrime/USCrime_training_data.txt";
        String outputfile2 = "/USCrime/USCrime_testing_data.txt";
        
        File sourceFile = new File(path + dataset);
        File outFile1 = new File(path + outputfile1);
        File outFile2 = new File(path + outputfile2);

        Integer num = 0, dim = 0;
        String line;
        
        ArrayList<Double[]> rawinput = new ArrayList<>();
        
        try {
        	BufferedReader br = new BufferedReader(new FileReader(sourceFile));
        	line = br.readLine();
        	String[] cache = line.split("\t");
        	num = Integer.valueOf(cache[0]);
        	dim = Integer.valueOf(cache[1]);
        	
        	
        	
        	while((line = br.readLine())!=null) {
        		cache = line.split("\t|,");
        		Double[] instance = new Double[dim+1];
        		for(int c = 1;c<cache.length;c++) {
        			instance[c-1] = Double.valueOf(cache[c]);
        		}
        		instance[cache.length-1] = Double.valueOf(cache[0]);
        		rawinput.add(instance);
        	}
        	
        	Collections.shuffle(rawinput);
        	
        }catch (IOException e) {
        	e.printStackTrace();
		}

        try {
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(outFile1.getAbsoluteFile()));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(outFile2.getAbsoluteFile()));
            
            Integer trainingnum = (int) Math.floor(num*0.75);
            Integer testingnum = num - trainingnum;
            
            writer1.write(trainingnum.toString()+"\t");
            writer1.write(dim.toString()+"\n");
            
            writer2.write(testingnum.toString()+"\t");
            writer2.write(dim.toString()+"\n");
            
            for(int i = 0; i<trainingnum; i++) {
            	for(int d = 0; d<dim; d++) {
            		writer1.write(rawinput.get(i)[d].toString());
            		writer1.write("\t");
            	}
            	writer1.write(rawinput.get(i)[dim].toString()+"\n");
            }
            writer1.close();
            
            for(int i = trainingnum; i<num; i++) {
            	for(int d = 0; d<dim; d++) {
            		writer2.write(rawinput.get(i)[d].toString());
            		writer2.write("\t");
            	}
            	writer2.write(rawinput.get(i)[dim].toString()+"\n");
            }
            writer2.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
}
