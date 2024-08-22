package rf.configtool.root.shell;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

import java.util.List;

public class ShellStore extends ShellCommand {

    @Override
    public String getName() {
        return "store";
    }
    @Override
    public String getBriefExampleParams() {
        return "name - stores Sys.lastResult under this name";
    }


    public Value execute(Ctx ctx, Command cmd) throws Exception {
        final String name=cmd.getCommandName();

        List<Arg> args=cmd.getArgs();
        if (args.size() != 1) throw new Exception(name + ": requires a simple name");

        String storeName=args.get(0).getString();

        // call LVStore:Store(storeName)
        String lambda="Lambda{LVStore:Store(P(1))}";
        Value[] lambdaArgs={
                new ValueString(storeName)
        };

        return callConfiguredLambda ("store", ctx, lambda, lambdaArgs);
    }


}
