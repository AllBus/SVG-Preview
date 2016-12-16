package com.kos.svgpreview.parser.svg

import Math._

/**
  * Created by Kos on 10.09.2016.
  */
case class NodeTransform(a: Float, b: Float, c: Float, d: Float, x: Float, y: Float) {


	def transformX(tx: Float, ty: Float) = a * tx + c * ty + x

	def transformY(tx: Float, ty: Float) = b * tx + d * ty + y

	def transform(tx: Float, ty: Float) = Array(transformX(tx, ty), transformY(tx, ty))

	def transformRX(tx: Float, ty: Float): Float = a * tx + c * ty

	def transformRY(tx: Float, ty: Float): Float = b * tx + d * ty

	def transformR(tx: Float, ty: Float) = Array(transformRX(tx, ty), transformRY(tx, ty))

	//todo: трансформация масштабирования, вобще говоря, не возможна
	def transformScale(ts: Float) = abs(transformSX(ts, 0))

	def transformSX(tx: Float, ty: Float): Float =
		(sqrt(a * a + b * b) * tx).toFloat //*(if(a>0)1else	-1)

	def transformSY(tx: Float, ty: Float): Float = (sqrt(d * d + c * c) * ty).toFloat * (
		if ((d > 0) && (a < 0) || (d < 0) && (a > 0)) -1 else 1)

	def getSkewY: Float = Math.atan2(b, a).toFloat

	def getSkewX: Float = Math.atan2(-c, d).toFloat

	def getRotation: Float = ((getSkewY * 180) / PI).toFloat


	/*
	|a c x
	|b d y
	|0 0 1

*/
	def apply(xp: Float, yp: Float) = new NodeTransform(a, b, c, d,
		x + a * xp + c * yp, b * xp + d * yp + y)

	//	x+xp,y+yp)

	def apply(arr: Array[Float]) = new NodeTransform(
		//		arr(0),arr(1),arr(2),arr(3),arr(4)+x,arr(5)+y)
		arr(0) * a + arr(1) * c,
		arr(0) * b + arr(1) * d,
		arr(2) * a + arr(3) * c,
		arr(2) * b + arr(3) * d,
		arr(4) * a + arr(5) * c + x,
		arr(4) * b + arr(5) * d + y)


	def mult(na: Double, nb: Double, nc: Double, nd: Double, nx: Double, ny: Double) =
		new NodeTransform(
			(na * a + nb * c).toFloat,
			(na * b + nb * d).toFloat,
			(nc * a + nd * c).toFloat,
			(nc * b + nd * d).toFloat,
			(nx * a + ny * c + x).toFloat,
			(nx * b + ny * d + y).toFloat)


	def scale(sx: Float, sy: Float) = new NodeTransform(
		a * sx, b * sx, c * sy, d * sy, x, y)

	//sx*a,sy*b,sx*c,sy*d,sx*x,sy*y)

	def rotate(angle: Float) = {
		val a = toRadians(angle)
		apply(Array[Float](cos(a).toFloat, sin(a).toFloat, -sin(a).toFloat, cos(a).toFloat, 0, 0))
	}

	def rotate(angle: Float, x: Float, y: Float) = {
		val a = toRadians(angle)
		apply(x,y).
		mult(cos(a), sin(a), -sin(a), cos(a), 0, 0).
		apply(-x,-y)
	}

	def skewX(angle:Float)={
		val a = toRadians(angle)
		mult(1,0,tan(a),1, 0, 0)
	}

	def skewY(angle:Float)={
		val a = toRadians(angle)
		mult(1,tan(a),0,1, 0, 0)
	}
}

object NodeTransform {
	def identity = new NodeTransform(1, 0, 0, 1, 0, 0)
}