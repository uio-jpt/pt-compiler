template TestTemplate {
    class Alpha {
    }

    class Beta extends Alpha {
    }

    class Gamma extends Beta {
    }

    class Delta extends Gamma {
    }
}

package TestPackage {
    inst TestTemplate with Alpha => Alpha_,
                           Beta => Beta_,
                           Gamma => Gamma_,
                           Delta => Delta_;
}
