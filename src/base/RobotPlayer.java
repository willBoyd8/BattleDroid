package base;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public strictfp class RobotPlayer {

    private Class<? extends Unit> hqBaseUnit;
    private Class<? extends Unit> minerBaseUnit;
    private Class<? extends Unit> refineryBaseUnit;
    private Class<? extends Unit> vaperatorBaseUnit;
    private Class<? extends Unit> designSchoolBaseUnit;
    private Class<? extends Unit> fulfillmentCenterBaseUnit;
    private Class<? extends Unit> landscaperBaseUnit;
    private Class<? extends Unit> deliveryDroneBaseUnit;
    private Class<? extends Unit> netGunBaseUnit;

    /**
     * Creates a new {@link RobotPlayer} instance with the given units as bases
     * @param hqBaseUnit the unit to use as the {@link RobotType#HQ}
     * @param minerBaseUnit the unit to use as the {@link RobotType#MINER}
     * @param refineryBaseUnit the unit to use as the {@link RobotType#REFINERY}
     * @param vaperatorBaseUnit the unit to use as the {@link RobotType#VAPORATOR}
     * @param designSchoolBaseUnit the unit to use as the {@link RobotType#DESIGN_SCHOOL}
     * @param fulfillmentCenterBaseUnit the unit to use as the {@link RobotType#FULFILLMENT_CENTER}
     * @param landscaperBaseUnit the unit to use as the {@link RobotType#LANDSCAPER}
     * @param deliveryDroneBaseUnit the unit to use as the {@link RobotType#DELIVERY_DRONE}
     * @param netGunBaseUnit the unit to use as the {@link RobotType#NET_GUN}
     */
    public RobotPlayer(
            Class<? extends Unit> hqBaseUnit,
            Class<? extends Unit> minerBaseUnit,
            Class<? extends Unit> refineryBaseUnit,
            Class<? extends Unit> vaperatorBaseUnit,
            Class<? extends Unit> designSchoolBaseUnit,
            Class<? extends Unit> fulfillmentCenterBaseUnit,
            Class<? extends Unit> landscaperBaseUnit,
            Class<? extends Unit> deliveryDroneBaseUnit,
            Class<? extends Unit> netGunBaseUnit
            ) {
        this.hqBaseUnit = hqBaseUnit;
        this.minerBaseUnit = minerBaseUnit;
        this.refineryBaseUnit = refineryBaseUnit;
        this.vaperatorBaseUnit = vaperatorBaseUnit;
        this.designSchoolBaseUnit = designSchoolBaseUnit;
        this.fulfillmentCenterBaseUnit = fulfillmentCenterBaseUnit;
        this.landscaperBaseUnit = landscaperBaseUnit;
        this.deliveryDroneBaseUnit = deliveryDroneBaseUnit;
        this.netGunBaseUnit = netGunBaseUnit;
    }

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public void run(RobotController rc) throws GameActionException, IllegalAccessException, InstantiationException {

        switch (rc.getType()) {
            case HQ:                 this.hqBaseUnit.newInstance().run(); break;
            case MINER:              this.minerBaseUnit.newInstance().run(); break;
            case REFINERY:           this.refineryBaseUnit.newInstance().run(); break;
            case VAPORATOR:          this.vaperatorBaseUnit.newInstance().run(); break;
            case DESIGN_SCHOOL:      this.designSchoolBaseUnit.newInstance().run(); break;
            case FULFILLMENT_CENTER: this.fulfillmentCenterBaseUnit.newInstance().run(); break;
            case LANDSCAPER:         this.landscaperBaseUnit.newInstance().run(); break;
            case DELIVERY_DRONE:     this.deliveryDroneBaseUnit.newInstance().run(); break;
            case NET_GUN:            this.netGunBaseUnit.newInstance().run(); break;
        }
    }
}