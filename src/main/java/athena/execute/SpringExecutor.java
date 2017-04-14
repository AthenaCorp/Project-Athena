package athena.execute;

public class SpringExecutor {
    public static void main(String[] args) {
        SpringConfigurator configurator = new SpringConfigurator();
        //executor.generateIndex();
        configurator.executePseudoRelevanceFeedback();
    }
}
