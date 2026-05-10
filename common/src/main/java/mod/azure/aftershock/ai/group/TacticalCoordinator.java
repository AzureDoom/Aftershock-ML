package mod.azure.aftershock.ai.group;

@FunctionalInterface
public interface TacticalCoordinator<E> {

    TacticalOrder getOrder(E mob, SquadBlackboard squad);
}
