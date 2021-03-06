import org.junit.Before
import org.junit.Test
import support.BikeWithInvokeMethod
import support.BikeWithMethodMissing
import support.Bike
import support.BikeBrokenException
import support.BikeWithMethodMissingAndInvokeMethod
import support.BikeWithMethodMissingThrowingEx

class POGOMethodDispatch extends MetaKoan {
    @Before
    void registerModifiedMetaClasses() {
        storeOriginalMetaClass(Bike)
        storeOriginalMetaClass(BikeWithInvokeMethod)
    }

    @Test
    void 'method declared directly in the class is executed (no surprise :))'() {
        def bike = new Bike()

        assert bike./*koanify*/ring/**/() == 'ring!'
    }

    @Test
    void 'method added to the POGOs metaclass can be executed as if it was defined in the POGO'() {
        Bike.metaClass.win = { 'won!' }
        def bike = new Bike()

        assert bike./*koanify*/win/**/() == 'won!'
    }

    @Test
    void 'method overridden in the metaclass takes precedence over the method defined in the class'() {
        Bike.metaClass.ring = { 'ring! (but differently)' }
        def bike = new Bike()

        assert bike.ring() == /*koanify*/'ring! (but differently)'/**/
    }

    @Test
    void 'property that is a closure is executed like a normal method'() {
        def bike = new Bike()

        assert bike./*koanify*/ride/**/() == 'riding!'
    }

    @Test
    void 'MissingMethodException is raised if a method is not found neither in the class nor in the metaclass'() {
        def bike = new Bike()

        shouldFail(/*koanify_as_ex*/MissingMethodException/**/) { bike.consumePetrol() }
    }

    @Test
    void 'if methodMissing is implemented it is executed if the called method is not found'() {
        def bike = new BikeWithMethodMissing()

        assert bike./*koanify*/ringLoudly/**/() == 'ring loudly!'
    }

    @Test
    void 'methodMissing is implemented but it throws a MissingMethodException if it cannot handle the unknown method'() {
        def bike = new BikeWithMethodMissing()

        shouldFail(/*koanify_as_ex*/MissingMethodException/**/) { bike.bell() }
        // Think: must methodMissing throw a MissingMethodException?
        // What would be the result if the methodMissing implementation hadn't thrown MissingMethodException?
    }

    @Test
    void 'if invokeMethod is overridden it is executed if the called method is not found'() {
        def bike = new BikeWithInvokeMethod()

        assert bike./*koanify*/rideOnTheMoon/**/() == 'riding onthemoon!'
    }

    @Test
    void 'invokeMethod is overridden but it should throw a MissingMethodException if it cannot handle the unknown method'() {
        def bike = new BikeWithInvokeMethod()

        shouldFail(/*koanify_as_ex*/MissingMethodException/**/) { bike.beRover() }
    }

    @Test
    void 'invokeMethod is never called if the method is implemented directly or in the metaclass'() {
        def bike = new BikeWithMethodMissingThrowingEx()
        bike.metaClass.win = { 'won!' }

        /*koanify_as_should_fail_or_not*/shouldFail/**/(BikeBrokenException) {
            bike.beRover()
        }
        /*koanify_as_should_fail_or_not*/shouldNotFail/**/(BikeBrokenException) {
            bike.ring()
            bike.win()
        }
    }

    @Test
    void 'only methodMissing is executed if both methodMissing and invokeMethod are implemented'() {
        def bike = new BikeWithMethodMissingAndInvokeMethod()

        /*koanify_as_should_fail_or_not*/shouldNotFail/**/(MissingMethodException) {
            bike.ringLoud()
        }
        /*koanify_as_should_fail_or_not*/shouldFail/**/(MissingMethodException) {
            bike.rideOnTheMoon()
        }
    }

    @Test
    void 'invokeMethod and methodMissing overridden in the metaclass takes precedence over methods defined in the class'() {
        BikeWithInvokeMethod.metaClass.invokeMethod = { String name, args ->
            'dynamic behaviour canceled'
        }
        BikeWithMethodMissing.metaClass.methodMissing = { String name, args ->
            'from metaclass'
        }
        def bikeWithInvokeMethod = new BikeWithInvokeMethod()
        def bikeWithMethodMissing = new BikeWithMethodMissing()

        assert bikeWithInvokeMethod.rideOnTheMoon() == /*koanify*/'dynamic behaviour canceled'/**/
        assert bikeWithMethodMissing.rideOnTheMoon() == /*koanify*/'from metaclass'/**/
        // Think: what is the behaviour for other non-special methods defined both in the class and in the metaclass?
    }
}
