package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;

import com.alibaba.dubbo.common.utils.IOUtils;

public class TestEl {
	public static void main(String[] args) throws IOException{
		int max = 12000;
		String[] seqList = IOUtils.readLines(new File("E:/seq.txt"));
		List[] result = new ArrayList[max];
		for(String seq: seqList){
			int s = Math.abs(seq.hashCode());
			int loc = s % max;
			if(result[loc]==null) result[loc] = new ArrayList();
			result[loc].add(seq);
		}
		Bag bag = new HashBag();
		for(int i=0;i<max;i++){
			bag.add(result[i]==null?0:result[i].size());
		}
		System.out.println(seqList.length / max + "avg, total:" + seqList.length);
		for(Object key: bag.uniqueSet()){
			System.out.println(key + "--->" + bag.getCount(key) + "=" + (((Integer)key) * bag.getCount(key)));	
		}
		
	}
    public static long hashCode(String str) {
    	long h = 0;
        long len = str.length();
	    char val[] = str.toCharArray();
        for (int i = 0; i < len; i++) {
            h = 31*h + val[i];
        }
        return h;
    }

}
