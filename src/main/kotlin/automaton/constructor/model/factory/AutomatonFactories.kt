package automaton.constructor.model.factory

fun getAllAutomatonFactories() = listOf<AutomatonFactory>(
    FiniteAutomatonFactory(),
    PushdownAutomatonFactory(),
    RegisterAutomatonFactory(),
    MealyMachineFactory(),
    TuringMachineFactory(),
    MultiTrackTuringMachineFactory(),
    MultiTapeTuringMachineFactory(),
    TuringMachineWithRegistersFactory()
)
