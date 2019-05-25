package com.aft.app.sc.huicent.entity;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@SuppressWarnings({"unused", "unchecked"})
public class FlightInfo implements Serializable {
	private static final long serialVersionUID = -1957042283024146736L;
	public String A;
	public String B;
	public String C;
	public String D;
	public String E;
	public String F;
	public ArrayList G;
	public SeatInfo H;
	public String I;
	public String J;
	public String K;
	public String L;
	public String M;
	public String a;
	public String b;
	public String c;
	public String d;
	public String e;
	public String f;
	public String g;
	public String h;
	public String i;
	public String j;
	public String k;
	public String l;
	public String m;
	public String n;
	public String o;
	public String p;
	public String q;
	public String r;
	public String s;
	public String t;
	public String u;
	public String v;
	public String w;
	public String x;
	public String y;
	public String z;

	public FlightInfo() {
		super();
		this.i = "";
		this.j = "";
		this.k = "";
	}

	public void A(String arg1) {
		this.x = arg1;
	}

	public String A() {
		return this.A;
	}

	public void B(String arg1) {
		this.z = arg1;
	}

	public String B() {
		return this.i;
	}

	public void C(String arg1) {
		this.E = arg1;
	}

	public String C() {
		return this.k;
	}

	public void D(String arg1) {
		this.F = arg1;
	}

	public String D() {
		return this.B;
	}

	public void E(String arg1) {
		this.A = arg1;
	}

	public String E() {
		return this.C;
	}

	public void F(String arg1) {
		this.i = arg1;
	}

	public void G(String arg1) {
		this.j = arg1;
	}

	public void H(String arg1) {
		this.k = arg1;
	}

	public void I(String arg1) {
		this.B = arg1;
	}

	public void J(String arg1) {
		this.C = arg1;
	}

	public void a(ArrayList arg1) {
		this.G = arg1;
	}

	public String a() {
		return this.M;
	}

	public void a(SeatInfo arg1) {
		this.H = arg1;
	}

	public void a(String arg1) {
		this.M = arg1;
	}

	public String b() {
		return this.L;
	}

	public void b(String arg1) {
		this.L = arg1;
	}

	public String c() {
		return this.D;
	}

	public void c(String arg1) {
		this.I = arg1;
	}

	public String d() {
		String string0 = this.y == null ? "" : this.y;
		return string0;
	}

	public void d(String arg1) {
		this.J = arg1;
	}

	public int describeContents() {
		return 0;
	}

	public String e() {
		return this.a;
	}

	public void e(String arg1) {
		this.D = arg1;
	}

	public void f(String arg1) {
		this.y = arg1;
	}

	public String f() {
		return this.b;
	}

	public void g(String arg1) {
		this.a = arg1;
	}

	public String g() {
		return this.c;
	}

	public void h(String arg1) {
		this.b = arg1;
	}

	public String h() {
		return this.e;
	}

	public void i(String arg1) {
		this.c = arg1;
	}

	public String i() {
		return this.f;
	}

	public void j(String arg1) {
		this.d = arg1;
	}

	public String j() {
		return this.g;
	}

	public void k(String arg1) {
		this.e = arg1;
	}

	public String k() {
		return this.h;
	}

	public void l(String arg1) {
		this.f = arg1;
	}

	public String l() {
		return this.m;
	}

	public void m(String arg1) {
		this.g = arg1;
	}

	public String m() {
		return this.n;
	}

	public void n(String arg1) {
		this.h = arg1;
	}

	public String n() {
		return this.o;
	}

	public void o(String arg1) {
		this.l = arg1;
	}

	public String o() {
		return this.p;
	}

	public void p(String arg1) {
		this.m = arg1;
	}

	public String p() {
		return this.q;
	}

	public void q(String arg1) {
		this.n = arg1;
	}

	public String q() {
		return this.r;
	}

	public void r(String arg1) {
		this.o = arg1;
	}

	public String r() {
		return this.s;
	}

	public void s(String arg1) {
		this.p = arg1;
	}

	public String s() {
		return this.t;
	}

	public void t(String arg1) {
		this.q = arg1;
	}

	public String t() {
		return this.u;
	}

	public void u(String arg1) {
		this.r = arg1;
	}

	public String u() {
		return this.v;
	}

	public void v(String arg1) {
		this.s = arg1;
	}

	public String v() {
		return this.w;
	}

	public void w(String arg1) {
		this.t = arg1;
	}

	public String w() {
		return this.x;
	}

	public void x(String arg1) {
		this.u = arg1;
	}

	public String x() {
		return this.z;
	}

	public void y(String arg1) {
		this.v = arg1;
	}

	public ArrayList y() {
		return this.G;
	}

	public void z(String arg1) {
		this.w = arg1;
	}

	public SeatInfo z() {
		return this.H;
	}
    
    public String toString() {
    	return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
