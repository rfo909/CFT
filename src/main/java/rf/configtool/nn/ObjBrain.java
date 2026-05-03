package rf.configtool.nn;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

public class ObjBrain extends Obj {
    private Brain brain;

    public ObjBrain(Brain brain) {
        this.brain=brain;
    }

    @Override
    public boolean eq(Obj x) {
        if (x==this) return true;
        return false;
    }

    @Override
    public String getTypeName() {
        return "Brain";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("[Brain]");
    }


}