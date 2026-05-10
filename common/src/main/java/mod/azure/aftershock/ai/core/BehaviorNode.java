package mod.azure.aftershock.ai.core;

@FunctionalInterface
public interface BehaviorNode<E> {

    BehaviorResult<E> tick(E mob, Blackboard blackboard, Cooldowns cooldowns);
}
