package com.aft.app.sc.a;

import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings({"unused", "unchecked"})
public class PostGenerator_c {
	public static String[] a(ArrayList arg11, String arg12, String arg13) throws Exception {
        String[] array_string;
        if(arg11 == null || arg11.size() < 6) {
            array_string = null;
        }
        else {
            Random random0 = new Random();
            int i = arg11.size();
            int[] array_i = new int[i];
            int i1;
            for(i1 = 0; i1 < array_i.length; ++i1) {
                array_i[i1] = i1 + 1;
            }

            for(i1 = i - 1; i1 >= 0; --i1) {
                int i2 = random0.nextInt(i1 + 1);
                int i3 = array_i[i2];
                array_i[i2] = array_i[i1];
                array_i[i1] = i3;
            }

            String string0 = "";
            int i4;
            for(i4 = 0; i4 < i; ++i4) {
                string0 = string0 + arg11.get(array_i[i4] - 1);
                if(i4 != i - 1) {
                    string0 = string0 + "&";
                }
            }

            String string1 = AESEncrypter_a.a(string0, KeyMaker_b.a(arg13, KeyMaker_b.a, arg12));
            String string2 = "";
            i4 = 0;
            while(i4 < 24) {
                if(i4 == 3) {
                    string0 = string2 + String.valueOf(array_i[0]);
                }
                else if(i4 == 5) {
                    string0 = string2 + String.valueOf(array_i[1]);
                }
                else if(i4 == 9) {
                    string0 = string2 + String.valueOf(array_i[2]);
                }
                else if(i4 == 11) {
                    string0 = string2 + String.valueOf(array_i[3]);
                }
                else if(i4 == 14) {
                    string0 = string2 + String.valueOf(array_i[4]);
                }
                else if(i4 == 20) {
                    string0 = string2 + String.valueOf(array_i[5]);
                }
                else {
                    string0 = string2 + String.valueOf(((int)(Math.random() * 10)));
                }

                ++i4;
                string2 = string0;
            }

            array_string = new String[]{string2, string1};
        }

        return array_string;
	}
}
