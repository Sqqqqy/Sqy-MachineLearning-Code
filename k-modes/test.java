package clustering;

import org.apache.commons.math3.distribution.LaplaceDistribution;
import java.io.IOException;
import java.util.ArrayList;

public class test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int k = 100;
		kmodes mode = new kmodes();
		mat test = mode.initdata();
		mode.repeat_lookfor(test, k);
	}
}
