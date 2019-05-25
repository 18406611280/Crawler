package com.aft.app.sc.huicent.entity;

import java.io.Serializable;

@SuppressWarnings({"unused", "unchecked"})
public class FlightQueryBean implements Serializable {
	private static final long serialVersionUID = 2983935639122016159L;
	private String a;
    private String b;
    private String c;
    private int d_isInter;
    private String e;
    private String f_depCity;
    private String g;
    private String h;
    private String i_desCity;
    private String j;
    private String k;
    public String l_time;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private String u;
    private String v;
    private String w;
    private int x;

    public FlightQueryBean() {
        super();
        this.x = 0;
    }




    public String a() {
        String string0 = this.a == null ? "" : this.a;
        return string0;
    }

    public void a(int arg1) {
        this.d_isInter = arg1;
    }

    public void a(String arg1) {
        this.a = arg1;
    }

    public String b() {
        String string0 = this.b == null ? "" : this.b;
        return string0;
    }

    public void b(int arg1) {
        this.x = arg1;
    }

    public void b(String arg1) {
        this.b = arg1;
    }

    public int c() {
        return this.d_isInter;
    }

    public void c(String arg1) {
        this.e = arg1;
    }

    public String d() {
        return this.e;
    }

    public void d(String arg1) {
        this.f_depCity = arg1;
    }

    public int describeContents() {
        return 0;
    }

    public String e() {
        return this.f_depCity;
    }

    public void e(String arg1) {
        this.g = arg1;
    }

    public String f() {
        return this.g;
    }

    public void f(String arg1) {
        this.h = arg1;
    }

    public String g() {
        return this.h;
    }

    public void g(String arg1) {
        this.i_desCity = arg1;
    }

    public String h() {
        return this.i_desCity;
    }

    public void h(String arg1) {
        this.j = arg1;
    }

    public String i() {
        return this.j;
    }

    public void i(String arg1) {
        this.k = arg1;
    }

    public String j() {
        return this.k;
    }

    public void j(String arg1) {
        this.l_time = arg1;
    }

    public String k() {
        return this.l_time;
    }

    public void k(String arg1) {
        this.m = arg1;
    }

    public String l() {
        return this.m;
    }

    public void l(String arg1) {
        this.n = arg1;
    }

    public String m() {
        return this.n;
    }

    public void m(String arg1) {
        this.o = arg1;
    }

    public String n() {
        return this.o;
    }

    public void n(String arg1) {
        this.p = arg1;
    }

    public String o() {
        return this.p;
    }

    public void o(String arg1) {
        this.q = arg1;
    }

    public String p() {
        String string0 = this.q == null ? "" : this.q;
        return string0;
    }

    public void p(String arg1) {
        this.r = arg1;
    }

    public String q() {
        String string0 = this.r == null ? "" : this.r;
        return string0;
    }

    public void q(String arg1) {
        this.s = arg1;
    }

    public String r() {
        return this.s;
    }

    public void r(String arg1) {
        this.u = arg1;
    }

    public String s() {
        String string0 = this.u == null ? "" : this.u;
        return string0;
    }

    public void s(String arg1) {
        this.v = arg1;
    }

    public String t() {
        return this.v;
    }

    public void t(String arg1) {
        this.w = arg1;
    }

    public String u() {
        return this.w;
    }

    public int v() {
        return this.x;
    }
}
