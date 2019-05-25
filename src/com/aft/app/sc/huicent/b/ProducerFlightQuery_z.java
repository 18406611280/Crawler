package com.aft.app.sc.huicent.b;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.entity.ContentProducer;

import com.aft.app.sc.HttpConstants;
import com.aft.app.sc.a.PostGenerator_c;
import com.aft.app.sc.huicent.entity.FlightQueryBean;

@SuppressWarnings({"unused", "unchecked"})
public class ProducerFlightQuery_z implements ContentProducer {
	public FlightQueryBean a_queryBean;
	public EntityTools_ct b_entityProducer;

	public ProducerFlightQuery_z(FlightQueryBean bean,
			EntityTools_ct entityTools) {
		super();
		a_queryBean = bean;
		b_entityProducer = entityTools;
	}

	public void writeTo(OutputStream arg8) throws IOException {
		DataOutputStream outPut = new DataOutputStream(arg8);
		ArrayList arrayList0 = new ArrayList();
		arrayList0.add(a_queryBean.c() + "");
		arrayList0.add(a_queryBean.e());
		arrayList0.add(a_queryBean.h());
		arrayList0.add(a_queryBean.k());
		arrayList0.add("sdal");
		arrayList0.add(String.valueOf(((int) (Math.random() * 1000000))));
		try {
			String[] array_string = PostGenerator_c.a(arrayList0,
					HttpConstants.rkey, HttpConstants.app_type);
			if (array_string != null) {
				a_queryBean.s(array_string[0]);
				a_queryBean.t(array_string[1]);
			}
		} catch (Exception exception0) {
			exception0.printStackTrace();
		}

		outPut.writeUTF("sdal");
		outPut.writeUTF(HttpConstants.app_type);
		outPut.writeUTF("HUICENT");
		outPut.writeUTF(HttpConstants.getTokenId());
//		outPut.writeUTF(HttpConstants.tokenID);
		outPut.writeUTF("SA1 V4.20.10");
		outPut.writeUTF(a_queryBean.b());
		outPut.writeUTF(a_queryBean.a());
		outPut.writeByte(20);
		outPut.writeUTF(a_queryBean.t());
		outPut.writeUTF(a_queryBean.u());
		outPut.writeUTF("SA1 V4.20.10");
		outPut.writeUTF(a_queryBean.d());
		outPut.writeUTF(a_queryBean.f());
		outPut.writeUTF(a_queryBean.g());
		outPut.writeUTF(a_queryBean.i());
		outPut.writeUTF(a_queryBean.j());
		outPut.writeUTF(a_queryBean.l());
		outPut.writeUTF(a_queryBean.m());
		outPut.writeUTF(a_queryBean.n());
		outPut.writeUTF(a_queryBean.o());
		outPut.writeUTF(a_queryBean.p());
		outPut.writeUTF(a_queryBean.q());
		outPut.writeUTF(a_queryBean.r());
		outPut.writeUTF(a_queryBean.s());
		outPut.flush();

		if (outPut != null) {
			outPut.close();
		}
	}

}
