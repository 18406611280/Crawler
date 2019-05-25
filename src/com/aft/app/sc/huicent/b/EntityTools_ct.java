package com.aft.app.sc.huicent.b;

import java.io.DataInputStream;
import java.util.ArrayList;

import com.aft.app.sc.huicent.entity.FlightInfo;
import com.aft.app.sc.huicent.entity.FlightQueryResult;
import com.aft.app.sc.huicent.entity.SeatInfo;

@SuppressWarnings({"unused", "unchecked"})
public class EntityTools_ct {

	public void ParseFlightQueryResult(DataInputStream input,
			FlightQueryResult flightQueryResult) throws Exception {
		FlightInfo flightInfo;
		int j;
		Exception excep = null;
		int i5;
		int i = 0;
		int resCode = input.readInt();
		if (resCode == 0) {
			flightQueryResult.g(input.readUTF());
			flightQueryResult.a(input.readUTF());
			flightQueryResult.b(input.readUTF());
			flightQueryResult.c(input.readUTF());
			flightQueryResult.d(input.readUTF());
			flightQueryResult.e(input.readUTF());
			flightQueryResult.f(input.readUTF());
			flightQueryResult.a(input.readInt());

			try {
				j = input.readInt();
			} catch (Exception e) {
				e.printStackTrace();
				excep = e;
				resCode = 0;
				j = resCode;
			}

			if (excep == null) {
				try {
					resCode = input.readInt();
				} catch (Exception e) {
					excep = e;
					resCode = 0;
				}
			}

			ArrayList flightList0 = new ArrayList();
			int i3;
			for (i3 = 0; i3 < j; ++i3) {
				FlightInfo flightInfo0 = new FlightInfo();
				flightInfo0.g(input.readUTF());
				flightInfo0.h(input.readUTF());
				flightInfo0.i(input.readUTF());
				flightInfo0.j(input.readUTF());
				flightInfo0.k(input.readUTF());
				flightInfo0.l(input.readUTF());
				flightInfo0.m(input.readUTF());
				flightInfo0.n(input.readUTF());
				flightInfo0.o(input.readUTF());
				flightInfo0.p(input.readUTF());
				flightInfo0.q(input.readUTF());
				flightInfo0.r(input.readUTF());
				flightInfo0.s(input.readUTF());
				flightInfo0.t(input.readUTF());
				flightInfo0.u(input.readUTF());
				flightInfo0.v(input.readUTF());
				flightInfo0.w(input.readUTF());
				flightInfo0.x(input.readUTF());
				flightInfo0.y(input.readUTF());
				flightInfo0.z(input.readUTF());
				flightInfo0.A(input.readUTF());
				flightInfo0.B(input.readUTF());
				flightInfo0.E(input.readUTF());
				flightInfo0.I(input.readUTF());
				flightInfo0.J(input.readUTF());
				ArrayList seatList0 = new ArrayList();
				int i4 = input.readInt();
				for (i5 = 0; i5 < i4; ++i5) {
					SeatInfo seatInfo0 = new SeatInfo();
					seatInfo0.b(input.readUTF());
					seatInfo0.c(input.readUTF());
					seatInfo0.d(input.readUTF());
					seatInfo0.e(input.readUTF());
					seatInfo0.f(input.readUTF());
					seatInfo0.g(input.readUTF());
					seatInfo0.h(input.readUTF());
					seatInfo0.i(input.readUTF());
					seatInfo0.j(input.readUTF());
					seatInfo0.k(input.readUTF());
					seatInfo0.l(input.readUTF());
					seatInfo0.m(input.readUTF());
					seatInfo0.n(input.readUTF());
					seatInfo0.o(input.readUTF());
					seatInfo0.p(input.readUTF());
					seatInfo0.q(input.readUTF());
					seatInfo0.r(input.readUTF());
					seatInfo0.a(input.readUTF());
					seatInfo0.s(input.readUTF());
					seatList0.add(seatInfo0);
				}

				flightInfo0.a(seatList0);
				flightList0.add(flightInfo0);
			}
			flightQueryResult.a(flightList0);

			ArrayList flightInfoList1 = new ArrayList();
			for (i5 = 0; i5 < resCode; ++i5) {
				FlightInfo flightInfo1 = new FlightInfo();
				flightInfo1.g(input.readUTF());
				flightInfo1.h(input.readUTF());
				flightInfo1.i(input.readUTF());
				flightInfo1.j(input.readUTF());
				flightInfo1.k(input.readUTF());
				flightInfo1.l(input.readUTF());
				flightInfo1.m(input.readUTF());
				flightInfo1.n(input.readUTF());
				flightInfo1.o(input.readUTF());
				flightInfo1.p(input.readUTF());
				flightInfo1.q(input.readUTF());
				flightInfo1.r(input.readUTF());
				flightInfo1.s(input.readUTF());
				flightInfo1.t(input.readUTF());
				flightInfo1.u(input.readUTF());
				flightInfo1.v(input.readUTF());
				flightInfo1.w(input.readUTF());
				flightInfo1.x(input.readUTF());
				flightInfo1.y(input.readUTF());
				flightInfo1.z(input.readUTF());
				flightInfo1.A(input.readUTF());
				flightInfo1.B(input.readUTF());
				flightInfo1.E(input.readUTF());
				flightInfo1.I(input.readUTF());
				flightInfo1.J(input.readUTF());
				ArrayList seatList1 = new ArrayList();
				int i6 = input.readInt();
				for (j = 0; j < i6; ++j) {
					SeatInfo seatInfo1 = new SeatInfo();
					seatInfo1.b(input.readUTF());
					seatInfo1.c(input.readUTF());
					seatInfo1.d(input.readUTF());
					seatInfo1.e(input.readUTF());
					seatInfo1.f(input.readUTF());
					seatInfo1.g(input.readUTF());
					seatInfo1.h(input.readUTF());
					seatInfo1.i(input.readUTF());
					seatInfo1.j(input.readUTF());
					seatInfo1.k(input.readUTF());
					seatInfo1.l(input.readUTF());
					seatInfo1.m(input.readUTF());
					seatInfo1.n(input.readUTF());
					seatInfo1.o(input.readUTF());
					seatInfo1.p(input.readUTF());
					seatInfo1.q(input.readUTF());
					seatInfo1.r(input.readUTF());
					seatInfo1.a(input.readUTF());
					seatInfo1.s(input.readUTF());
					seatList1.add(seatInfo1);
				}

				flightInfo1.a(seatList1);
				flightInfoList1.add(flightInfo1);
			}
			flightQueryResult.b(flightInfoList1);

		} else if (resCode == 2) {
			j = input.readInt();
			ArrayList list = new ArrayList();
			for (resCode = 0; resCode < j; ++resCode) {
				flightInfo = new FlightInfo();
				flightInfo.C(input.readUTF());
				flightInfo.D(input.readUTF());
				list.add(flightInfo);
			}

			flightQueryResult.a(list);
			j = input.readInt();
			list = new ArrayList();
			for (resCode = 0; resCode < j; ++resCode) {
				flightInfo = new FlightInfo();
				flightInfo.C(input.readUTF());
				flightInfo.D(input.readUTF());
				list.add(flightInfo);
			}

			flightQueryResult.b(list);
			resCode = input.readInt();
			ArrayList arrayList5 = new ArrayList();
			while (i < resCode) {
				FlightInfo flightInfo3 = new FlightInfo();
				flightInfo3.C(input.readUTF());
				flightInfo3.D(input.readUTF());
				arrayList5.add(flightInfo3);
				++i;
			}

			flightQueryResult.c(arrayList5);
		} else if (resCode == 4 || resCode == 5 || resCode == 9){
			flightQueryResult.status = input.readUTF();
			flightQueryResult.errorMsg = input.readUTF();
		}
		closeInput(input);
	}

	public void closeInput(DataInputStream in) throws Exception {
		if (in != null) {
			in.close();
		}
	}
}
