package nju.java;

import nju.java.creatures.Creatures;
import nju.java.creatures.bad.Bad;
import nju.java.creatures.good.Good;
import nju.java.creatures.good.GourdDolls;
import nju.java.creatures.good.Grandpa;
import nju.java.creatures.bad.ScorpionKing;
import nju.java.creatures.bad.SnakeQueen;
import nju.java.creatures.bad.Toad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import static nju.java.Ground.Status.*;

/**
 * Created by cbcwestwolf on 2017/12/26.
 */
public class Ground extends JPanel {

    public static final int STEP = 10; // 每次移动的距离
    public static final int SPACE = 8*STEP ; // 图片的边长   (必须是STEP的整数倍）
    public static final int DISTANCE = 4*STEP; // 攻击范围
    public static final int TIME_CLOCK = 100; // 线程休眠时间 （毫秒）
    public static final int PIXEL_HEIGHT = 720; // 上下的高度（像素点）
    public static final int PIXEL_WIDTH = 1280; // 左右的长度
    public static final int MAX_X = (PIXEL_WIDTH-SPACE) / STEP ;
    public static final int MAX_Y = (PIXEL_HEIGHT-SPACE) / STEP ;

    private static boolean stop; // 玩家是否按下暂停键
    private static Status status = WELCOME; // 4种状态：未开始，打斗中，回放中

    private Image backgroundImage = null; // 背景图片

    // 爷爷是唯一的
    private Grandpa grandpa = null;
    private GourdDolls [] gourdDolls = null;
    private ArrayList<Good>  goodCreatures = null;

    // 蝎子精和蛇精是唯一的
    private SnakeQueen snake = null;
    private ScorpionKing scorpion = null;
    private Toad[] toads = null; // 小马仔们
    private ArrayList<Bad> badCreatures = null;

    private ArrayList<Creatures> deadCreatures = null; // 记录死亡的生物
    private ArrayList<Thread> creaturesThreads = new ArrayList<Thread>();;

    private Timer timer ;
    private ActionListener timerTask ;

    public Ground(){
        addKeyListener(new TAdapter());// 添加键盘监视器
        setFocusable(true); // 设置可见

        loadGround(); // 装载场景

        initCreature(); // 初始化生物
        initTimer();
        actionCreature();
    }



    public static boolean isStop() {
        return stop;
    }

    public static void setStop(boolean stop) {
        Ground.stop = stop;
    }

    public static Status getStatus() {
        return status;
    }

    public static void setStatus(Status status) {
        Ground.status = status;
    }

    // Creatures API
    public ArrayList<Good> getGoodCreatures(){
        return goodCreatures;
    }

    public ArrayList<Bad> getBadCreatures(){
        return badCreatures;
    }

    private void loadGround(){
        // 背景分辨率为 1280*720 , 即16:9 。 每个格子的边长为80分辨率
        URL loc = this.getClass().getClassLoader().getResource("背景2.png");
        ImageIcon iia = new ImageIcon(loc); // Image是抽象类，所以只能通过ImageIcon来创建
        backgroundImage = iia.getImage();

    }

    private void initCreature(){

        // 初始化爷爷
        grandpa = new Grandpa(0,MAX_Y/2,this);
        grandpa.setImage("爷爷.png");

        // 初始化葫芦娃
        gourdDolls = new GourdDolls[7]; // 默认为鹤翼阵型
        gourdDolls[0] = new GourdDolls(0,1*4,this);
        gourdDolls[1] = new GourdDolls(1*SPACE/STEP,2*SPACE/STEP,this);
        gourdDolls[2] = new GourdDolls(2*SPACE/STEP,3*SPACE/STEP,this);
        gourdDolls[3] = new GourdDolls(3*SPACE/STEP,4*SPACE/STEP,this);
        gourdDolls[4] = new GourdDolls(2*SPACE/STEP,5*SPACE/STEP,this);
        gourdDolls[5] = new GourdDolls(1*SPACE/STEP,6*SPACE/STEP,this);
        gourdDolls[6] = new GourdDolls(0,7*SPACE/STEP,this);
        gourdDolls[0].setImage("大娃.png");
        gourdDolls[1].setImage("二娃.png");
        gourdDolls[2].setImage("三娃.png");
        gourdDolls[3].setImage("四娃.png");
        gourdDolls[4].setImage("五娃.png");
        gourdDolls[5].setImage("六娃.png");
        gourdDolls[6].setImage("七娃.png");

        // 把爷爷和葫芦娃添加到队列中
        goodCreatures = new ArrayList<Good>();
        goodCreatures.add(grandpa);
        for( GourdDolls g : gourdDolls )
            goodCreatures.add(g);

        // 初始化蛇精
        snake = new SnakeQueen(MAX_X,MAX_Y/2-SPACE/STEP,this);
        snake.setImage("蛇精.png");

        // 初始化蝎子精
        scorpion =  new ScorpionKing(MAX_X,MAX_Y/2+SPACE/STEP,this);
        scorpion.setImage("蝎子精.png");

        toads = new Toad[7];
        for(int i = 0 ; i < 7 ; ++ i){

            if( i != 3 && i != 5)
                toads[i] = new Toad(MAX_X,i*SPACE/STEP,this);
            else if (i == 3 )
                toads[i] = new Toad(MAX_X,7*SPACE/STEP,this);
            else
                toads[i] = new Toad(MAX_X,8*SPACE/STEP,this);

            toads[i].setImage("蛤蟆精.png");
        }

        badCreatures = new ArrayList<Bad>();
        badCreatures.add(snake);
        badCreatures.add(scorpion);
        for(Bad c : toads)
            badCreatures.add(c);

        deadCreatures = new ArrayList<Creatures>();
    }

    public void resetCreature(){

        deadCreatures.clear();
        goodCreatures.clear();
        badCreatures.clear();

        // 重置爷爷
        grandpa.setBlood(100);
        grandpa.setImage("爷爷.png");
        grandpa.setX(0);
        grandpa.setY(MAX_Y/2);

        // 重置葫芦娃
        for( GourdDolls g : gourdDolls ){
            g.setBlood(100);
        }
        gourdDolls[0].setX(0); gourdDolls[0].setX(4);
        gourdDolls[1].setX(1*SPACE/STEP); gourdDolls[0].setX(2*SPACE/STEP);
        gourdDolls[2].setX(2*SPACE/STEP); gourdDolls[0].setX(3*SPACE/STEP);
        gourdDolls[3].setX(3*SPACE/STEP); gourdDolls[0].setX(4*SPACE/STEP);
        gourdDolls[4].setX(2*SPACE/STEP); gourdDolls[0].setX(5*SPACE/STEP);
        gourdDolls[5].setX(1*SPACE/STEP); gourdDolls[0].setX(6*SPACE/STEP);
        gourdDolls[6].setX(0); gourdDolls[0].setX(7*SPACE/STEP);
        gourdDolls[0].setImage("大娃.png");
        gourdDolls[1].setImage("二娃.png");
        gourdDolls[2].setImage("三娃.png");
        gourdDolls[3].setImage("四娃.png");
        gourdDolls[4].setImage("五娃.png");
        gourdDolls[5].setImage("六娃.png");
        gourdDolls[6].setImage("七娃.png");

        // 把爷爷和葫芦娃添加到队列中
        goodCreatures.add(grandpa);
        for( GourdDolls g : gourdDolls )
            goodCreatures.add(g);

        // 重置蛇精
        snake.setBlood(100);
        snake.setImage("蛇精.png");
        snake.setX(MAX_X);
        snake.setY(MAX_Y/2-SPACE/STEP);

        // 重置蝎子精
        scorpion.setBlood(100);
        scorpion.setImage("蝎子精.png");
        scorpion.setX(MAX_X);
        scorpion.setY(MAX_Y/2+SPACE/STEP);

        // 重置蛤蟆精
        for(int i = 0 ; i < 7 ; ++ i){

            toads[i].setX(MAX_X);
            if( i != 3 && i != 5)
                toads[i].setY(i*SPACE/STEP);
            else if (i == 3 )
                toads[i].setY(7*SPACE/STEP);
            else
                toads[i].setY(8*SPACE/STEP);

            toads[i].setImage("蛤蟆精.png");
            toads[i].setBlood(100);
        }


        badCreatures.add(snake);
        badCreatures.add(scorpion);
        for(Bad c : toads)
            badCreatures.add(c);

    }

    private void initTimer(){
        timerTask = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO:添加更多定时检查
                checkCreature();
            }
        };
        timer = new Timer(TIME_CLOCK,timerTask);
        timer.start();
    }

    private void actionCreature(){

        for(Creatures c : goodCreatures)
            creaturesThreads.add(new Thread(c));
        for(Creatures c : badCreatures)
            creaturesThreads.add(new Thread(c));
        for(Thread t : creaturesThreads)
            t.start();
    }

    // 检查两个Creatures列表,将死了的生物拖到deadCreatures中。如果出现一方已经死亡，暂停游戏
    private synchronized void checkCreature(){

        /*System.out.println("检查生物:3个列表中的生物个数为:"
                +goodCreatures.size()+" "
                +badCreatures.size()+" "
                +deadCreatures.size());*/
        if( goodCreatures.isEmpty() || badCreatures.isEmpty() ){
            status = WELCOME;
            System.out.println("状态转为BEGIN");
            // TODO:弹出游戏信息提示
            return ;
        }

        Iterator<Good> g = goodCreatures.iterator();
        while(g.hasNext()){
            Good temp = g.next();
            if(temp.isDead()){
                temp.setImage("葫芦娃墓碑.png");
                deadCreatures.add(temp);
                g.remove();
            }
        }

        Iterator<Bad> b = badCreatures.iterator();
        while(b.hasNext()){
            Bad temp = b.next();
            if(temp.isDead()){
                temp.setImage("妖怪墓碑.png");
                deadCreatures.add(temp);
                b.remove();
            }
        }

    }

    private void paintGround(Graphics g){


        g.drawImage(backgroundImage,0,0, PIXEL_WIDTH, PIXEL_HEIGHT,this);

        for( Good c : goodCreatures )
            g.drawImage(c.getImage(),c.getX()*STEP,c.getY()*STEP,SPACE,SPACE,this);

        for( Bad c : badCreatures)
            g.drawImage(c.getImage(),c.getX()*STEP,c.getY()*STEP,SPACE,SPACE,this);
        for( Creatures c : deadCreatures )
            g.drawImage(c.getImage(),c.getX()*STEP,c.getY()*STEP,SPACE,SPACE,this);

    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        paintGround(g);

    }

    @Override
    public void repaint(){

        super.repaint();
    }

    public enum Status {WELCOME, FIGHTING, REPLAYING , FINISHED};

    class TAdapter extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();

            if(key == KeyEvent.VK_SPACE){ // 开始
                if( status == WELCOME){
                    status = FIGHTING;
                    System.out.println("状态从BEGIN转为FIGHTING");
                }
                else if ( status == FINISHED ){
                    status = WELCOME;
                    resetCreature();
                    actionCreature();
                    // TODO:重绘
                    System.out.println("状态从FIGHTING转为WELCOME");
                }
            }
            else if(key == KeyEvent.VK_S){ // 回放
                if( status == WELCOME){
                    status = REPLAYING;
                    System.out.println("状态从BEGIN转为REPLAYING");
                }
            }
            else if(key == KeyEvent.VK_P){ // 暂停
                stop = !stop;
                if(stop)
                    System.out.println("暂停！");
                else
                    System.out.println("解除暂停！");
            }

            //System.out.println("Status="+status.toString()+" isStop="+stop);

            repaint();
        }
    }

    // Creature API : 攻击成功返回boolean
    // 检查的重点：距离
    public boolean requireAttack(Creatures attacker, Creatures attacked){
        int distance = distance(attacker,attacked);
        if( distance > 0 && distance <= DISTANCE/STEP ){ // 可以位移
            // 对双方的血量进行减少
            int attackerBlood = attacker.getBlood()-attacked.getPower()/2;
            int attackedBlood = attacked.getBlood()-attacker.getPower();
            System.out.println("攻击者血量降为"+attackerBlood+" 被攻击者血量降为"+attackedBlood);
            attacker.setBlood(attackerBlood);
            attacked.setBlood(attackedBlood);
            return true;
        }
        else
            return false;
    }

    // Creature API : 攻击成功返回boolean
    // 检查的重点：是否重合，是否越界，是否只有一个值为1
    public boolean requireWalk(Creatures c, int x_off, int y_off){
        if( x_off * y_off != 0 || Math.abs(x_off+y_off) != 1 )
            return false;
        int newX = c.getX()+x_off, newY = c.getY() + y_off;
        if(newX < 0 || newX > MAX_X || newY<0 || newY > MAX_Y )
            return false;
        for(Creatures i : goodCreatures ){
            if(i == c)
                continue;
            if( i.getX() == newX && i.getY() == newY ){
                return false;
            }
        }
        for( Creatures i : badCreatures){
            if(i == c)
                continue;
            if( i.getX() == newX && i.getY() == newY ){
                return false;
            }
        }
        c.setX(newX);
        c.setY(newY);
        return true;
    }

    // Creatures API:
    // 返回两个生物体在坐标轴上的距离(x距离+y距离
    public final int distance(Creatures a, Creatures b){
        int minX = a.getX()<b.getX() ? a.getX():b.getX();
        int maxX = a.getX()<b.getX() ? b.getX():a.getX();
        int minY = a.getY()<b.getY() ? a.getY():b.getY();
        int maxY = a.getY()<b.getY() ? b.getY():a.getY();
        return maxX - minX + maxY - minY;
    }

}
