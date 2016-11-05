package eu.cuteapps.camerahttp.myutils;

//	Min + (Math.random() * ((Max - Min) + 1))
//	This is the pattern to get a random number within a(n) (only positive?) range

public class MathUtils {
		
		// Get Random double in the range 0 - MAX_VALUE
		public static double getRandomPositiveDouble(double MAX_VALUE) {	
			double RANDOM_DOUBLE = 0 + Math.random() * ((MAX_VALUE - 0) + 1);
			return RANDOM_DOUBLE;
		}
		
		// Get Random double in the POSITIVE range MIN_VALUE - MAX_VALUE
		public static double getRandomDoubleInPositiveBounds(double MIN_VALUE, double MAX_VALUE) {		
			double RANDOM_DOUBLE = MIN_VALUE + Math.random() * ((MAX_VALUE - MIN_VALUE) + 1);
			return RANDOM_DOUBLE;
		}
		
		// Get Random double in the range (-MAX_VALUE, MAX_VALUE)
		public static double getRandomlySignedDouble(double MAX_VALUE) {
			double RANDOM_DOUBLE = getRandomPositiveDouble(MAX_VALUE);
			if(Math.random() * 2 < 1) { return -RANDOM_DOUBLE; }	
			return RANDOM_DOUBLE;
		}
		
		public void bar() {
			
		}
		
		public static double[] toGreekGrid(double lat, double lng) {	
            lat = lat * Math.PI / 180;
            lng = lng * Math.PI / 180;
            double a = 6378137;
            double b = 6356752.31424518;
            double e2 = (Math.pow(a,2) - Math.pow(b,2)) / Math.pow(a, 2);
            double v = a / Math.sqrt(1 - e2 * (Math.sin(lat) * Math.sin(lat)));
            double X = v * Math.cos(lat) * Math.cos(lng);
            double Y = v * Math.cos(lat) * Math.sin(lng);
            double Z = (v * (1 - e2)) * Math.sin(lat);
            double px = 199.723;
            double py = -74.03;
            double pz = -246.018;
            double rw = 0;
            double rf = 0;
            double rk = 0;
            double ks = 1;
            double c1 = Math.cos(rw);
            double c2 = Math.cos(rf);
            double c3 = Math.cos(rk);
            double s1 = Math.sin(rw);
            double s2 = Math.sin(rf);
            double s3 = Math.sin(rk);
            double D11 = c2 * c3;
            double D21 = -c2 * s3;
            double D31 = Math.sin(rf);
            double D12 = s1 * s2 * c3 + c1 * s3;
            double D22 = -s1 * s2 * s3 + c1 * c3;
            double D32 = -s1 * c2;
            double D13 = -c1 * s2 * c3 + s1 * s3;
            double D23 = c1 * s2 * s3 + s1 * c3;
            double D33 = c1 * c2;
            double X1 = px + ks * (D11 * X + D12 * Y + D13 * Z);
            double Y1 = py + ks * (D21 * X + D22 * Y + D23 * Z);
            double Z1 = pz + ks * (D31 * X + D32 * Y + D33 * Z);
            lng = Math.atan2(Y1, X1);
            double lat0 = Math.atan2(Z1, Math.sqrt(X1 * X1 + Y1 * Y1));
            b = 6356752.31414036;
            e2 = (Math.pow(a, 2) - Math.pow(b,2)) / Math.pow(a, 2);
            while (Math.abs(lat - lat0) > 0.0000000001) {
            	
                double No = a / Math.sqrt(1 - e2 * Math.sin(lat0) * Math.sin(lat0));
                double h = Math.sqrt(X1 * X1 + Y1 * Y1) / Math.cos(lat0) - No;
                lat = lat0;
                lat0 = Math.atan(Z1 / Math.sqrt(X1 * X1 + Y1 * Y1) * (1 / (1 - e2 * No / (No+ h))));
            }
            lng = lng - 24 * Math.PI / 180;
            double m0 = 0.9996;
            double es2 = (Math.pow(a,2) - Math.pow(b,2)) / (Math.pow(b,2));
            double V = Math.sqrt(1 + es2 * Math.cos(lat) * Math.cos(lat));
            double eta = Math.sqrt(es2 * Math.cos(lat) * Math.cos(lat));
            double Bf = Math.atan(Math.tan(lat) / Math.cos(V * lng) * (1 + eta * eta / 6 * (1 - 3 * Math.sin(lat) * Math.sin(lat)) * lng * lng * lng * lng));
            double Vf = Math.sqrt(1 + es2 * Math.cos(Bf) * Math.cos(Bf));
            double etaf = Math.sqrt(es2 * Math.cos(Bf) * Math.cos(Bf));
            double n = (a - b) / (a + b);
            double r1 = (1 + n * n / 4 + n * n * n * n / 64) * Bf;
            double r2 = 3.0 / 2.0 * n * (1 - n * n / 8) * Math.sin(2 * Bf);
            double r3 = 15.0 / 16.0 * n * n * (1 - n * n / 4) * Math.sin(4 * Bf);
            double r4 = 35.0 / 48.0 * n * n * n * Math.sin(6 * Bf);
            double r5 = 315.0 / 512.0 * n * n * n * n * Math.sin(8 * Bf);
            double Northing = ((a / (1 + n))) * (r1 - r2 + r3 - r4 + r5) * m0 - 0.001 + 4202812 - 4207988.1206046063;
            double ys = Math.tan(lng) * Math.cos(Bf) / Vf * (1 + etaf * etaf * lng * lng * Math.cos(Bf) * Math.cos(Bf) * (etaf * etaf / 6 + lng * lng / 10));
            ys = Math.log(ys + Math.sqrt(ys * ys + 1));
            double Easting = m0 * Math.pow(a,2) / b * ys + 500000;
            return new double[] { Easting, Northing };
        }
		
		// function - low pass filter that smoothens sensor values
		public static float[] lowPassFilter(float current[], float last[], float a) {
			float[] smooth = new float[3];
				for(int i = 0; i < 3; i ++) {
					smooth[i] = last[i] * (1.0f - a) + current[i] * a;
				}
				return smooth;
		}
		
}