package automaton.constructor.model.factory

fun getAllAutomatonFactories() = listOf<AutomatonFactory>(
    FiniteAutomatonFactory(),
    PushdownAutomatonFactory(),
    RegisterAutomatonFactory(),
    MealyMooreMachineFactory(),
    TuringMachineFactory(),
    MultiTrackTuringMachineFactory(),
    MultiTapeTuringMachineFactory(),
    TuringMachineWithRegistersFactory()
)
