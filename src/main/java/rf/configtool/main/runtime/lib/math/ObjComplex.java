package rf.configtool.main.runtime.lib.math;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.IsSynthesizable;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;

/******************************************************************************
 *  Compilation:  javac Complex.java
 *  Execution:    java Complex
 *
 *  Data type for complex numbers.
 *
 *  The data type is "immutable" so once you create and initialize
 *  a Complex object, you cannot change it. The "final" keyword
 *  when declaring re and im enforces this rule, making it a
 *  compile-time error to change the .re or .im instance variables after
 *  they've been initialized.
 *
 *  % java Complex
 *  a            = 5.0 + 6.0i
 *  b            = -3.0 + 4.0i
 *  Re(a)        = 5.0
 *  Im(a)        = 6.0
 *  b + a        = 2.0 + 10.0i
 *  a - b        = 8.0 + 2.0i
 *  a * b        = -39.0 + 2.0i
 *  b * a        = -39.0 + 2.0i
 *  a / b        = 0.36 - 1.52i
 *  (a / b) * b  = 5.0 + 6.0i
 *  conj(a)      = 5.0 - 6.0i
 *  |a|          = 7.810249675906654
 *  tan(a)       = -6.685231390246571E-6 + 1.0000103108981198i
 *
 ******************************************************************************/

 // https://introcs.cs.princeton.edu/java/97data/Complex.java


public class ObjComplex extends Obj implements IsSynthesizable {
    private final double re;   // the real part
    private final double im;   // the imaginary part

    // create a new object with the given real and imaginary parts
    public ObjComplex(double real, double imag) {
        re = real;
        im = imag;

        this.add(new FunctionReal());
        this.add(new FunctionImag());
        this.add(new FunctionAbs());
        this.add(new FunctionPhase());
        this.add(new FunctionAdd());
        this.add(new FunctionSubtract());
        this.add(new FunctionMultiply());
        this.add(new FunctionDivide());
        this.add(new FunctionScale());
        this.add(new FunctionConjugate());
        this.add(new FunctionReciprocal());
        this.add(new FunctionExp());
        this.add(new FunctionSin());
        this.add(new FunctionCos());
        this.add(new FunctionTan());
    }

 
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Math.Complex";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Math.Complex";
    }
    
    private ObjComplex theObj () {
        return this;
    }
    
    @Override
    public String createCode() throws Exception {
        return "Std.Math.Complex(" 
            + new java.math.BigDecimal(re).toPlainString()
            + ","
            + new java.math.BigDecimal(im).toPlainString()
            + ")";
    }



    class FunctionReal extends Function {
        public String getName() {
            return "real";
        }
        public String getShortDesc() {
            return "real() - returns float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueFloat(re);
        }
    }

    class FunctionImag extends Function {
        public String getName() {
            return "imag";
        }
        public String getShortDesc() {
            return "imag() - returns float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueFloat(im);
        }
    }



    class FunctionAbs extends Function {
        public String getName() {
            return "abs";
        }
        public String getShortDesc() {
            return "abs() - returns float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            double val = Math.hypot(re, im);
            return new ValueFloat(val);
        }
    }


    class FunctionPhase extends Function {
        public String getName() {
            return "phase";
        }
        public String getShortDesc() {
            return "phase() - returns phase in range -pi to pi";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            double val=Math.atan2(im, re);
            return new ValueFloat(val);
        }
    }


    private ObjComplex getComplex(String name, List<Value> params, int pos) throws Exception {
        Obj obj=getObj(name, params, pos);
        if (obj instanceof ObjComplex) return (ObjComplex) obj;
        throw new Exception("Expected Complex value for " + name);
    }


    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }
        public String getShortDesc() {
            return "add(complex) - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected complex value");
            ObjComplex a = theObj();
            ObjComplex b = getComplex("complex",params,0);

            double real = a.re + b.re;
            double imag = a.im + b.im;
            return new ValueObj(new ObjComplex(real, imag));
        }
    }


    class FunctionSubtract extends Function {
        public String getName() {
            return "subtract";
        }
        public String getShortDesc() {
            return "subtract(complex) - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected complex value");
            ObjComplex a = theObj();
            ObjComplex b = getComplex("complex",params,0);

            double real = a.re - b.re;
            double imag = a.im - b.im;
            return new ValueObj(new ObjComplex(real, imag));
        }
    }

    class FunctionMultiply extends Function {
        public String getName() {
            return "multiply";
        }
        public String getShortDesc() {
            return "multiply(complex) - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected complex value");
            ObjComplex b = getComplex("complex",params,0);

            return new ValueObj(times(b));
        }
    }


    class FunctionDivide extends Function {
        public String getName() {
            return "divide";
        }
        public String getShortDesc() {
            return "divide(complex) - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected complex value");
            ObjComplex a = theObj();
            ObjComplex b = getComplex("complex",params,0);
            return new ValueObj(divides(b));
        }
    }



    class FunctionScale extends Function {
        public String getName() {
            return "scale";
        }
        public String getShortDesc() {
            return "scale(double) - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected complex value");
            ObjComplex a = theObj();
            double alpha=getFloat("alpha",params,0);

            double real = alpha * a.re;
            double imag = alpha * a.im;

            return new ValueObj(new ObjComplex(real, imag));
        }
    }


    class FunctionConjugate extends Function {
        public String getName() {
            return "conjugate";
        }
        public String getShortDesc() {
            return "conjugate() - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(conjugate());
        }
    }

    class FunctionReciprocal extends Function {
        public String getName() {
            return "reciprocal";
        }
        public String getShortDesc() {
            return "reciprocal() - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(reciprocal());
        }
    }

    
    class FunctionExp extends Function {
        public String getName() {
            return "exp";
        }
        public String getShortDesc() {
            return "exp() - returns complex exponential of this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(exp());
        }
    }


    class FunctionSin extends Function {
        public String getName() {
            return "sin";
        }
        public String getShortDesc() {
            return "sin() - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(sin());
        }
    }


    class FunctionCos extends Function {
        public String getName() {
            return "cos";
        }
        public String getShortDesc() {
            return "cos() - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(cos());
        }
    }

    class FunctionTan extends Function {
        public String getName() {
            return "tan";
        }
        public String getShortDesc() {
            return "tan() - returns complex";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(tan());
        }
    }



    // ##########

    // return a new Complex object whose value is (this * b)
    public ObjComplex times(ObjComplex b) {
        ObjComplex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new ObjComplex(real, imag);
    }



    // return a new Complex object whose value is the conjugate of this
    public ObjComplex conjugate() {
        return new ObjComplex(re, -im);
    }

    // return a new Complex object whose value is the reciprocal of this
    public ObjComplex reciprocal() {
        double scale = re*re + im*im;
        return new ObjComplex(re / scale, -im / scale);
    }
    

  
    // return a / b
    public ObjComplex divides(ObjComplex b) {
        ObjComplex a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public ObjComplex exp() {
        return new ObjComplex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    // return a new Complex object whose value is the complex sine of this
    public ObjComplex sin() {
        return new ObjComplex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex cosine of this
    public ObjComplex cos() {
        return new ObjComplex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex tangent of this
    public ObjComplex tan() {
        return sin().divides(cos());
    }


}