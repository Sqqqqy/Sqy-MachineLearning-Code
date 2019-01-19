package clustering;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;


public class kmodes {
	/*
	 * tag:			聚类
	 * means:		k-modes
	 * data-set:	UCI mushroom	
	 * time:		2019/1/19
	 * 
	 * */
	public mat initdata() throws IOException{
		/*
		 * 
		 *初始化数据 
		 *相应ASCII码
		 *缺失数据用0补全
		 *
		 * */
		BufferedReader in = new BufferedReader(new FileReader("E:\\developer\\mushroom.txt"));
		mat data = new mat();
		data.label = new ArrayList<Integer>();
		data.feature = new ArrayList<ArrayList>();
		String buf;
		while((buf = in.readLine()) != null){
			int tmpx = 0;
			String[] tmp = buf.split(",");
			data.label.add(-1);
			ArrayList<Integer> arr = new ArrayList<Integer>();
			for(int i = 0;i < tmp.length;i ++){
				if(tmp[i].toCharArray()[0] == '?') tmpx = 0;
				else tmpx = (int)tmp[i].toCharArray()[0]-(int)('a');
				arr.add(tmpx);
			}				
			data.feature.add(arr);
		}
		in.close();
		return data;
	}
	
	private mat initkcluster(mat data,int k,int len){
		/*
		 * 随机生成k个中心点
		 * k
		 * */
		ArrayList<ArrayList> k_mat = new ArrayList<ArrayList>();
		ArrayList<Integer> label_mat = new ArrayList<Integer>();
		int x;
		for(int i = 0;i < k;i ++){
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for(int j = 0;j < len;j ++){
				tmp.add((int)(Math.random()*23));
			}
			label_mat.add((int)(Math.random()*k));
			k_mat.add(tmp);
		}
		mat returnSize = new mat();
		returnSize.feature = k_mat;
		returnSize.label = label_mat;
		return returnSize;
	}
	
	private int compare(ArrayList<Integer> L1,ArrayList<Integer> L2){
		/*
		 * 比较数组里元素相同的个数
		 * 
		 * */
		int cnt = 0;
		for(int i = 0;i < L1.size();i ++){
			if(L1.get(i).equals(L2.get(i))) cnt++;
		}
		return cnt;
	}
	
	private static double getNoise(double param,double sentive) {
		/*
		 * 运用拉普拉斯函数制造噪声
		 * 
		 * */
		Random random = new Random();
		double randomDouble = random.nextDouble() - 0.5;
		double noise = -(sentive / param) * Math.signum(randomDouble)
				* Math.log(1 - 2 * Math.abs(randomDouble));
		return noise;
	}
	
	private ArrayList<ArrayList> cal_dis(ArrayList<ArrayList> feature_mat,ArrayList<ArrayList> c,int k){
		/*
		 * 计算数据点与每个中心点的距离
		 * */
		ArrayList<ArrayList> dis_k = new ArrayList<ArrayList>();
		int len = feature_mat.size();
		for(int i = 0;i < k;i ++){
			ArrayList<Float> tmp = new ArrayList<Float>();
			for(int j =0 ;j < len;j ++){
				float size = (float)compare(feature_mat.get(j),c.get(i));
				tmp.add((float) (size+getNoise(0.1,24*100)/(23+getNoise(0.1,24*100))));
			}
			dis_k.add(tmp);
		}
		return dis_k;
	}
	
	private ArrayList<Integer> classify(ArrayList<ArrayList> dis,int k){
		/*
		 * 通过比较到各个中心点的距离给数据点分类
		 * ！！！！(数据点数目均分)
		 * 
		 * */
		ArrayList<Integer> return_label = new ArrayList<Integer>();
		int []cnt = new int [k+2];
		int []flag = new int [k+2];
		int index;
		for(int i =0 ;i < cnt.length;i ++){
			cnt[i] = 0;
			flag[i] = 0;
		}
		for(int j = 0;j < dis.get(0).size();j ++){
			float min = 1000000.0f;
			index = -1;
			for(int i = 0;i < k;i ++){
				if((float)dis.get(i).get(j) < min){
					min = (float)dis.get(i).get(j);
					index = i;
				}
			}
			if(cnt[index] >(float)dis.get(0).size()/(float)k)
				flag[index] = 1;
			while(flag[index] == 1){
				 index = (index+1)%k;
			}
			cnt[index] ++;
			return_label.add(index+1);
		}
		return return_label;
	}
	
	private int cal_zs(ArrayList<Integer> nums){
		/*
		 * 计算众数
		 * 
		 * */
		int count = 0;
        int candidate = 0;
        for(int i = 0; i < nums.size(); ++i){
          if(count == 0){
             candidate = (int)nums.get(i).intValue();  
          }         
          if(nums.get(i).equals(candidate))
           ++count;
         else
            --count;
        }
        return candidate;
	}
	
	private mat updata_c0(ArrayList<ArrayList> feature_mat,ArrayList<Integer> label_fixed_mat,int k){
		/*
		 * 对每个类数据点分类后单个属性值的众数作为新的中心的每一维的值
		 * 
		 * */
		mat returnMat = new mat();
		returnMat.feature = new ArrayList<ArrayList>();
		returnMat.label = new ArrayList<Integer>();
		for(int i = 0;i < k;i ++){
			returnMat.label.add(i);
		}
		for(int m = 0; m < k;m ++){
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for(int j = 0;j < feature_mat.get(0).size();j ++){
				ArrayList<Integer> t = new ArrayList<Integer>();
				for(int i = 0;i < feature_mat.size();i ++){
					if(label_fixed_mat.get(i).equals(m+1))
						t.add((int)feature_mat.get(i).get(j));
				}
				int max = cal_zs(t);
				tmp.add(max);
			}
			returnMat.feature.add(tmp);
		}
		return returnMat;
	}
	
	public void repeat_lookfor(mat test,int k) throws IOException{
		/*
		 * 反复迭代(100times)使中心点不再变化
		 * !!!(第一次的中心点是随机生成的,其后计算众数更新)
		 * */
		mat c0 = initkcluster(test,k, test.feature.get(0).size());
		ArrayList<Integer> label_fixed = new ArrayList<Integer>();
		ArrayList<ArrayList> dis =  new ArrayList<ArrayList>();
		mat updated_mat = new mat();
		for(int i = 0;i < 100; i ++){
			dis = cal_dis(test.feature, c0.feature, k);
			label_fixed = classify(dis, k);
			updated_mat = updata_c0(test.feature, label_fixed, k);
			c0 = updated_mat;
		}
		System.out.println(c0.feature);
		System.out.print(label_fixed);
	}
	
}
