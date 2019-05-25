package com.aft.app.sc.huicent.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@SuppressWarnings({"unused", "unchecked"})
public class SeatInfo implements Serializable {
	private static final long serialVersionUID = 8338056523592968891L;
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
    
    public SeatInfo() {
        super();
    }
    
    public void a(String arg1) {
        this.r = arg1;
    }

    public String a() {
        return this.r;
    }

    public void b(String arg1) {
        this.a = arg1;
    }

    public String b() {
        return this.a;
    }

    public void c(String arg1) {
        this.b = arg1;
    }

    public String c() {
        return this.b;
    }

    public void d(String arg1) {
        this.c = arg1;
    }

    public String d() {
        return this.c;
    }

    public int describeContents() {
        return 0;
    }

    public void e(String arg1) {
        this.d = arg1;
    }

    public String e() {
        return this.d;
    }

    public void f(String arg1) {
        this.e = arg1;
    }

    public String f() {
        return this.e;
    }

    public void g(String arg1) {
        this.f = arg1;
    }

    public String g() {
        return this.f;
    }

    public void h(String arg1) {
        this.g = arg1;
    }

    public String h() {
        return this.g;
    }

    public void i(String arg1) {
        this.h = arg1;
    }

    public String i() {
        return this.h;
    }

    public void j(String arg1) {
        this.i = arg1;
    }

    public String j() {
        String string0 = this.i == null ? "" : this.i;
        return string0;
    }

    public void k(String arg1) {
        this.j = arg1;
    }

    public String k() {
        return this.k;
    }

    public void l(String arg1) {
        this.k = arg1;
    }

    public String l() {
        return this.l;
    }

    public void m(String arg1) {
        this.l = arg1;
    }

    public String m() {
        return this.m;
    }

    public void n(String arg1) {
        this.m = arg1;
    }

    public String n() {
        return this.n;
    }

    public void o(String arg1) {
        this.n = arg1;
    }

    public String o() {
        return this.o;
    }

    public void p(String arg1) {
        this.o = arg1;
    }

    public String p() {
        return this.p;
    }

    public void q(String arg1) {
        this.p = arg1;
    }

    public String q() {
        return this.q;
    }

    public void r(String arg1) {
        this.q = arg1;
    }

    public String r() {
        return this.s;
    }

    public void s(String arg1) {
        this.s = arg1;
    }
    
    public String toString() {
    	return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
