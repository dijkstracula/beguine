import org.junit.jupiter.api.Test;

public class TopologyTests {
    @Test
    public void trivialTopology() {
        new TopologyBuilder()
                .withMaxNodes(3)
                .build(); /* Goes nowhere, does nothing */
    }
}
