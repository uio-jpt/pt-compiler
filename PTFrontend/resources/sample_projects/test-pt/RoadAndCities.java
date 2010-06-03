package RoadAndCities {
    inst Graph with Node => City;
    inst fieldtemplate with Field => City;

    class City adds {
        int getValue() {
            return super[Node].getValue() + super[Field].getValue();
        }
    }
}
