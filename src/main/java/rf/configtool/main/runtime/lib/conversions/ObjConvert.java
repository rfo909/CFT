/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main.runtime.lib.conversions;


import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;

/**
 * Conversions of all kinds
 */
public class ObjConvert extends Obj {

    
    private static Conv mul(String name, double factor) {
        return new ConvMul(name, factor);
    }
    
    private static Conv add(String name, double value) {
        return new ConvAdd(name, value);
    }

    private static final double c=299792.458; // km/s
    private static final double au=149597870.7; // km
    
    private static double ly = c*86400*365; // km

    private static Conv conversions[]= {
        mul("psiToBar",     0.0689475729),
        mul("barToPsi", 1.0/0.0689475729),
        add("celsiusToKelvin", 273.15),
        add("kelvinToCelsius", -273.15),
        new ConvCelsiusFahrenheit(),
        new ConvFahrenheitCelsius(),
        mul("meterToFeet", 3.280839895),
        mul("feetToMeter", 1.0 / 3.280839895),
        mul("feetToInches", 12.0),
        mul("inchesToFeet", 1.0/12.0),
        mul("mileToKm", 1.609344),
        mul("kmToMile", 1.0/1.609344),
        mul("literToGallon", 0.264172052),
        mul("gallonToLiter", 1.0/0.264172052),
        mul("kgToLbs", 2.20462262),
        mul("lbsToKg", 1.0/2.20462262),
        mul("inchToCm", 2.54),
        mul("cmToInch", 1.0/2.54),
        mul("calToJoule", 4.18400),
        mul("jouleToCal", 1.0/4.18400),

        mul("poundToKg", 0.45359237),
        mul("kgToPound", 1.0/0.45359237),
        
        mul("stoneToKg", 6.35029318),
        mul("kgToStone", 1.0/6.35029318),
        
        

        mul("auToKm", au),
        mul("kmToAu", 1.0/au),
        mul("lyToKm", ly),
        mul("kmToLy", 1.0/ly),

        mul("lyToAu", ly/au),
        mul("auToLy", au/ly),
};
    
    
    
    
    public ObjConvert() {
        Function[] arr=new Function[conversions.length];
        for (int i=0; i<conversions.length; i++) {
            arr[i]=new FunctionCommon(conversions[i]);
        }
        setFunctions(arr);

//        for (Conv conv:conversions) {
//            this.add(new FunctionCommon(conv));
//        }
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Convert";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Convert";
    }
    
    class FunctionCommon extends Function {
        private Conv conv;
        public FunctionCommon(Conv conv) {
            this.conv=conv;
        }
        public String getName() {
            return conv.getName();
        }
        public String getShortDesc() {
            return conv.getName() + "(value)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected single parameter (value)");
            double x=getFloat("value", params, 0);  // int or float ok
            return new ValueFloat(conv.getResult(x));
        }
    }
    

}
