package automaton.constructor.model.factory

fun getAllAutomatonFactories() = listOf<AutomatonFactory>(
    FiniteAutomatonFactory(),
    PushdownAutomatonFactory(),
    RegisterAutomatonFactory(),
    MealyMachineFactory(),
    MooreMachineFactory(),
    TuringMachineFactory(),
    MultiTrackTuringMachineFactory(),
    MultiTapeTuringMachineFactory(),
    TuringMachineWithRegistersFactory()
)
