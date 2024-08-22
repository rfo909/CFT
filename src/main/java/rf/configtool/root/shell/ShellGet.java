package rf.configtool.root.shell;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

import java.util.List;

public class ShellGet extends ShellCommand {

    @Override
    public String getName() {
        return "get";
    }
    @Override
    public String getBriefExampleParams() {
        return "name - get stored value";
    }


    public Value execute(Ctx ctx, Command cmd) throws Exception {
        final String name=cmd.getCommandName();

        List<Arg> args=cmd.getArgs();
        if (args.size() != 1) throw new Exception(name + ": requires a simple name");

        String storeName=args.get(0).getString();

        // call LVStore:Get(storeName)
        String lambda="Lambda{LVStore:Get(P(1))}";
        Value[] lambdaArgs={
                new ValueString(storeName)
        };

        return callConfiguredLambda ("get", ctx, lambda, lambdaArgs);
    }


}
