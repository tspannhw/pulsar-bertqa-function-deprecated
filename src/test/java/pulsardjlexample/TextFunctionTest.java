package pulsardjlexample;

import dev.pulsarfunction.pulsardjlexample.TextFunction;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.common.io.SourceConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.junit.Assert;
import org.junit.Test;
import org.apache.pulsar.functions.api.Context;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class TextFunctionTest {

    protected Context ctx;

    protected void init(Context ctx) {
        this.ctx = ctx;
    }

    protected void log(String msg) {
        if (ctx != null && ctx.getLogger() != null) {
            ctx.getLogger().info(String.format("Function: [%s, id: %s, instanceId: %d of %d] %s",
                    ctx.getFunctionName(), ctx.getFunctionId(), ctx.getInstanceId(), ctx.getNumInstances(), msg));
        }
    }

    @Test
    public void testTextFunction() {
        TextFunction func = new TextFunction();
        String output = func.process("this is great.", mock(Context.class));
        Assert.assertEquals(output, "Positive");
    }

    /**
     * --auto-ack true --jar pulsardjlexample-1.0.jar
     * --classname "dev.pulsarfunction.pulsardjlexample.TextFunction"
     * --dead-letter-topic chatdead --inputs "persistent://public/default/chat"   --log-topic "persistent://public/default/chatlog" --name TextProcess --namespace default --output "persistent://public/default/chatresult" --tenant public  --max-message-retries 5
     *
     * @param args
     * @throws Exception
     */
        public static void main(String[] args) throws Exception {

            FunctionConfig functionConfig = FunctionConfig.builder()
                    .className(TextFunction.class.getName())
                    .inputs(Collections.singleton("persistent://public/default/chat"))
                    .name("textFunction")
                    .tenant("public")
                    .namespace("default")
                    .runtime(FunctionConfig.Runtime.JAVA)
                    .cleanupSubscription(true)
                    .build();

            LocalRunner localRunner = LocalRunner.builder()
                    .brokerServiceUrl("pulsar://nvidia-desktop:6650")
                    .functionConfig(functionConfig)
                    .build();

            localRunner.start(false);

            Thread.sleep(30000);
            localRunner.stop();
            System.exit(0);
        }
}
