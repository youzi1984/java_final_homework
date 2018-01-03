package nju.java.creatures.bad;

import nju.java.Ground;
import nju.java.creatures.Creatures;
import nju.java.creatures.good.Good;
import nju.java.tools.Status;

import java.util.ArrayList;

import static nju.java.ConstantValue.*;

/**
 * Created by cbcwestwolf on 2017/12/28.
 */
public abstract class Bad extends Creatures {
    public Bad(int x, int y, Ground ground) {
        super(x, y, ground);
    }

    public ArrayList<Good> getAttackable() {
        ArrayList<Good> all = this.ground.getGoodCreatures();
        ArrayList<Good> result = new ArrayList<Good>();

        for (Good g : all) {
            if (distance(this, g) <=  DISTANCE / STEP )
                result.add(g);
        }
        return result;
    }

    // 寻找距离最近的敌人
    public Good getNearestGood() {
        ArrayList<Good> all = this.ground.getGoodCreatures();
        int minDistance = Integer.MAX_VALUE;
        Good result = null;
        for (Good a : all) {
            if (distance(this, a) < minDistance) {// TODO:判断
                minDistance = distance(this, a);
                result = a;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "反方";
    }

    public void run() {

        while (!Thread.interrupted()) {
            if (Ground.getStatus() == Status.FIGHTING) {
                try {
                    if (isDead() || Ground.isStop() || Ground.getStatus() != Status.FIGHTING) {
                        //System.out.println("没状态？");
                        Thread.sleep(TIME_CLOCK);
                        continue;
                    }

                    ArrayList<Good> goods = getAttackable();
                    int maxValue = 0;
                    Good goal = null;
                    if (!goods.isEmpty()) {
                        // 找到得分高的
                        for (Good g : goods)
                            if (attackValue(g) > maxValue) {
                                maxValue = attackValue(g);
                                goal = g;
                            }
                    }
                    if (goal != null)
                        this.ground.requireAttack(this, goal);
                    else {
                        // 找到距离近的
                        Good g = getNearestGood();
                        int x_off = g.getX() - this.getX();
                        int y_off = g.getY() - this.getY();
                        if (Math.abs(x_off) > Math.abs(y_off)) {
                            if (x_off > 0)
                                this.ground.requireWalk(this, 1, 0);
                            else
                                this.ground.requireWalk(this, -1, 0);
                        } else {
                            if (y_off > 0)
                                this.ground.requireWalk(this, 0, 1);
                            else
                                this.ground.requireWalk(this, 0, -1);
                        }
                    }

                    Thread.sleep(TIME_CLOCK);

                } catch (Exception e) {

                }
            } else if (Ground.getStatus() ==    Status.REPLAYING) {

                try {
                    Thread.sleep(0,TIME_CLOCK/REPLAY_CLOCK);

                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
