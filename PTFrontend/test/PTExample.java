template TrafficSimulation { // PTTemplate (PTDecl)
    class Automobile { // SimpleClass
        int speed;
        Automobile next;
    }
    class Car extends Automobile {
        int numOfSeats;
    }
    class Lorry extends Automobile {
        int length;
    }
}

package BusinessTrafficSimulation { //PTPackage (PTDecl)
    /*
    inst T1 with C1 => M; //PTInstDecl
    //           ^^^^^^^ PTDummyClass
    //           OrgID=source, ID=target
    inst T2 with C2 => M, MergeClass => M; //PTInstDecl
    */
    inst TrafficSimulation with Automobile => Vehicle (next -> nextVehicle), Car => PrivateCar, Lorry => BusinessCar;

    class Vehicle adds {
        String brand;
    }
    class PrivateCar adds {
        int luggageVolume;
    }
    class BusinessCar adds {
        int loadCapacity;
    }

        // goal 1
        // baade A og B har f.. => feilmelding!

        // goal 2
        /*
        void f() {
            super[A].f()
            super[B].f()
        }
        */
        // goal 3
        // fiks renaming ala A => X (k -> y)

 }
