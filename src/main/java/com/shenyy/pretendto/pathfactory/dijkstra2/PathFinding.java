package com.shenyy.pretendto.pathfactory.dijkstra2;

import com.shenyy.pretendto.pathfactory.*;
import com.shenyy.pretendto.pathfactory.enumtype.AlgoType;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathFinding {

    //FRAME
    JFrame frame;
    //GENERAL VARIABLES
    public int cells = 20;
    public int delay = 30;
    public double dense = .5;
    public double density = (cells * cells) * .5;
    public int startx = -1;
    public int starty = -1;
    public int finishx = -1;
    public int finishy = -1;
    public int tool = 0;
    public int checks = 0;
    public double length = 0;
    public int curAlg = 0;
    private int WIDTH = 1100;
    private final int HEIGHT = 650;
    private final int MSIZE = 600;
    private int CSIZE = MSIZE / cells;

    //VARIABLES——ACO
    public double alpha = 1;
    public double beta = 2;
    public double rho = 0.2;

    //UTIL ARRAYS
    private String[] algorithms = {"Dijkstra", "A*", "RRT", "RRT*", "ACO", "GA"};
    private String[] tools = {"Start", "Finish", "Wall", "Eraser"};
    //BOOLEANS
    public boolean solving = false;
    //UTIL
    public Node[][] map;

    //RRT* Node List
    public List<com.shenyy.pretendto.pathfactory.algo.Node> nodeList = new ArrayList<>();
    //draw final path with line
    public List<com.shenyy.pretendto.pathfactory.algo.Node> linePath = new ArrayList<>();

    PathFactory<Point, Obstacle<Point>> staticPathFactory;
    Random r = new Random();
    //SLIDERS
    JSlider size = new JSlider(1, 5, 2);
    JSlider speed = new JSlider(0, 500, delay);
    JSlider obstacles = new JSlider(1, 100, 50);
    //SLIDERS——ACO
    JSlider alphaSL = new JSlider(0, 100, 3);
    JSlider betaSL = new JSlider(0, 100, 20);
    JSlider rhoSL = new JSlider(2, 5, 2);
    //LABELS
    JLabel algL = new JLabel("Algorithms");
    JLabel toolL = new JLabel("Toolbox");
    JLabel sizeL = new JLabel("Size:");
    JLabel cellsL = new JLabel(cells + "x" + cells);
    JLabel delayL = new JLabel("Delay:");
    JLabel msL = new JLabel(delay + "ms");
    JLabel obstacleL = new JLabel("Dens:");
    JLabel densityL = new JLabel(obstacles.getValue() + "%");
    JLabel checkL = new JLabel("Checks: " + checks);
    JLabel lengthL = new JLabel("Path Length: " + length);
    //LABELS——ACO
    JLabel param1L = new JLabel("alpha");
    JLabel param2L = new JLabel("beta");
    JLabel param3L = new JLabel("rho");
    JLabel alphaValueL = new JLabel(String.valueOf(alpha));
    JLabel betaValueL = new JLabel(String.valueOf(beta));
    JLabel rhoValueL = new JLabel(String.valueOf(rho));
    //BUTTONS
    JButton searchB = new JButton("Start Search");
    JButton resetB = new JButton("Reset");
    JButton genMapB = new JButton("Generate Map");
    JButton clearMapB = new JButton("Clear Map");
    JButton creditB = new JButton("Credit");
    //DROP DOWN
    JComboBox algorithmsBx = new JComboBox(algorithms);
    JComboBox toolBx = new JComboBox(tools);
    //PANELS
    JPanel toolP = new JPanel();
    JPanel algoInfoP = new JPanel();
    //CANVAS
    Map canvas;
    //BORDER
    Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    private static PathFinding instance;

//    public static void main(String[] args) {    //MAIN METHOD
//        getInstance();
//        instance.clearMap();
//        instance.initialize();
//    }

    public PathFinding() {    //CONSTRUCTOR
    }

    public static PathFinding getInstance() {
        if (instance == null) {
            instance = new PathFinding();
        }
        return instance;
    }

    public void generateMap() {    //GENERATE MAP
        clearMap();    //CREATE CLEAR MAP TO START
        for (int i = 0; i < density; i++) {
            Node current;
            do {
                int x = r.nextInt(cells);
                int y = r.nextInt(cells);
                current = map[x][y];    //FIND A RANDOM NODE IN THE GRID
            } while (current.getType() == 2);    //IF IT IS ALREADY A WALL, FIND A NEW ONE
            current.setType(2);    //SET NODE TO BE A WALL
        }
    }

    public void clearMap() {    //CLEAR MAP
        finishx = -1;    //RESET THE START AND FINISH
        finishy = -1;
        startx = -1;
        starty = -1;
        map = new Node[cells][cells];    //CREATE NEW MAP OF NODES
        for (int x = 0; x < cells; x++) {
            for (int y = 0; y < cells; y++) {
                map[x][y] = new Node(3, x, y);    //SET ALL NODES TO EMPTY
            }
        }
        //RRT* nodes
        nodeList.clear();
        linePath.clear();
        reset();    //RESET SOME VARIABLES
    }

    public void resetMap() {    //RESET MAP
        for (int x = 0; x < cells; x++) {
            for (int y = 0; y < cells; y++) {
                Node current = map[x][y];
                if (current.getType() == 4 || current.getType() == 5)    //CHECK TO SEE IF CURRENT NODE IS EITHER CHECKED OR FINAL PATH
                    map[x][y] = new Node(3, x, y);    //RESET IT TO AN EMPTY NODE
            }
        }
        if (startx > -1 && starty > -1) {    //RESET THE START AND FINISH
            map[startx][starty] = new Node(0, startx, starty);
            map[startx][starty].setHops(0);
        }
        if (finishx > -1 && finishy > -1)
            map[finishx][finishy] = new Node(1, finishx, finishy);
        //RRT* nodes
        nodeList.clear();
        linePath.clear();
        reset();    //RESET SOME VARIABLES
    }

    public void initialize() {    //INITIALIZE THE GUI ELEMENTS
        frame = new JFrame();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(WIDTH, HEIGHT);
        frame.setTitle("Path Finding");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        toolP.setBorder(BorderFactory.createTitledBorder(loweredetched, "Controls"));
        int space = 25;
        int buff = 45;

        toolP.setLayout(null);
        toolP.setBounds(10, 10, 210, 600);

        searchB.setBounds(40, space, 120, 25);
        toolP.add(searchB);
        space += buff;

        resetB.setBounds(40, space, 120, 25);
        toolP.add(resetB);
        space += buff;

        genMapB.setBounds(40, space, 120, 25);
        toolP.add(genMapB);
        space += buff;

        clearMapB.setBounds(40, space, 120, 25);
        toolP.add(clearMapB);
        space += 40;

        algL.setBounds(40, space, 120, 25);
        toolP.add(algL);
        space += 25;

        algorithmsBx.setBounds(40, space, 120, 25);
        toolP.add(algorithmsBx);
        space += 40;

        toolL.setBounds(40, space, 120, 25);
        toolP.add(toolL);
        space += 25;

        toolBx.setBounds(40, space, 120, 25);
        toolP.add(toolBx);
        space += buff;

        sizeL.setBounds(15, space, 40, 25);
        toolP.add(sizeL);
        size.setMajorTickSpacing(10);
        size.setBounds(50, space, 100, 25);
        toolP.add(size);
        cellsL.setBounds(160, space, 40, 25);
        toolP.add(cellsL);
        space += buff;

        delayL.setBounds(15, space, 50, 25);
        toolP.add(delayL);
        speed.setMajorTickSpacing(5);
        speed.setBounds(50, space, 100, 25);
        toolP.add(speed);
        msL.setBounds(160, space, 40, 25);
        toolP.add(msL);
        space += buff;

        obstacleL.setBounds(15, space, 100, 25);
        toolP.add(obstacleL);
        obstacles.setMajorTickSpacing(5);
        obstacles.setBounds(50, space, 100, 25);
        toolP.add(obstacles);
        densityL.setBounds(160, space, 100, 25);
        toolP.add(densityL);
        space += buff;

        checkL.setBounds(15, space, 100, 25);
        toolP.add(checkL);
        space += buff;

        lengthL.setBounds(15, space, 120, 25);
        toolP.add(lengthL);
        space += buff;

        creditB.setBounds(40, space, 120, 25);
        toolP.add(creditB);

        /**ACO panel*/
        algoInfoP.setLayout(null);
        algoInfoP.setBounds(850, 10, 200, 600);
        algoInfoP.setBorder(BorderFactory.createTitledBorder(loweredetched, "Info"));
        alphaSL.setMajorTickSpacing(10);

        space = 25;
        param1L.setBounds(15, space, 40, 25);
        algoInfoP.add(param1L);
        alphaSL.setMajorTickSpacing(10);
        alphaSL.setBounds(50, space, 100, 25);
        algoInfoP.add(alphaSL);
        alphaValueL.setBounds(160, space, 40, 25);
        algoInfoP.add(alphaValueL);
        space += buff;
        param2L.setBounds(15, space, 40, 25);
        algoInfoP.add(param2L);
        betaSL.setMajorTickSpacing(10);
        betaSL.setBounds(50, space, 100, 25);
        algoInfoP.add(betaSL);
        betaValueL.setBounds(160, space, 40, 25);
        algoInfoP.add(betaValueL);
        space += buff;
        param3L.setBounds(15, space, 40, 25);
        algoInfoP.add(param3L);
        rhoSL.setMajorTickSpacing(10);
        rhoSL.setBounds(50, space, 100, 25);
        algoInfoP.add(rhoSL);
        rhoValueL.setBounds(160, space, 40, 25);
        algoInfoP.add(rhoValueL);


        frame.getContentPane().add(toolP);
        frame.getContentPane().add(algoInfoP);

        canvas = new Map();
        canvas.setBounds(230, 10, MSIZE + 1, MSIZE + 1);
        frame.getContentPane().add(canvas);

        searchB.addActionListener(new ActionListener() {        //ACTION LISTENERS
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                if ((startx > -1 && starty > -1) && (finishx > -1 && finishy > -1)) {
                    solving = true;
                }
            }
        });
        resetB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetMap();
                Update();
            }
        });
        genMapB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateMap();
                Update();
            }
        });
        clearMapB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearMap();
                Update();
            }
        });
        algorithmsBx.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                curAlg = algorithmsBx.getSelectedIndex();
                Update();
            }
        });
        toolBx.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                tool = toolBx.getSelectedIndex();
            }
        });
        size.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cells = size.getValue() * 10;
                clearMap();
                reset();
                Update();
            }
        });
        speed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                delay = speed.getValue();
                Update();
            }
        });
        obstacles.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                dense = (double) obstacles.getValue() / 100;
                Update();
            }
        });
        creditB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "	                         Pathfinding\n"
                        + "             Copyright (c) 2017-2018\n"
                        + "                         Greer Viau\n"
                        + "          Build Date:  March 28, 2018   ", "Credit", JOptionPane.PLAIN_MESSAGE, new ImageIcon(""));
            }
        });
        //ACO
        alphaSL.addChangeListener((e) -> {
            alpha = alphaSL.getValue() / 10;
            Update();
        });
        betaSL.addChangeListener((e) -> {
            beta = betaSL.getValue() / 10;
            Update();
        });
        rhoSL.addChangeListener((e) -> {
            rho = (double) rhoSL.getValue() / 10;
            Update();
        });

        startSearch();    //START STATE
    }

    public void startSearch() {    //START STATE
        if (solving) {
            switch (curAlg) {
                case 0:
//                    Alg.Dijkstra();
                    staticPathFactory = new StaticPathFactory<>(null, null, null, AlgoType.DIJKSTRA, null);
                    Path path = staticPathFactory.createStaticPath2D();
                    path.construct();
                    solving = false;
                    break;
                case 1:
//                    Alg.AStar();
                    staticPathFactory = new StaticPathFactory<>(null, null, null, AlgoType.A_STAR, null);
                    path = staticPathFactory.createStaticPath2D();
                    path.construct();
                    solving = false;
                    break;
                case 2:
                    staticPathFactory = new StaticPathFactory<>(null, null, null, AlgoType.RRT, null);
                    path = staticPathFactory.createStaticPath2D();
                    path.construct();
                    solving = false;
                    break;
                case 3:
                    staticPathFactory = new StaticPathFactory<>(null, null, null, AlgoType.RRT_STAR, null);
                    path = staticPathFactory.createStaticPath2D();
                    path.construct();
                    solving = false;
                    break;
                case 4:
                    staticPathFactory = new StaticPathFactory<>(null, null, null, AlgoType.ACO, null);
                    path = staticPathFactory.createStaticPath2D();
                    path.construct();
                    solving = false;
                    break;
                case 5:
                    staticPathFactory = new StaticPathFactory<>(null, null, null, AlgoType.GA, null);
                    path = staticPathFactory.createStaticPath2D();
                    path.construct();
                    solving = false;
                    break;
            }
        }
        pause();    //PAUSE STATE
    }

    public void pause() {    //PAUSE STATE
        int i = 0;
        while (!solving) {
            i++;
            if (i > 500)
                i = 0;
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
        startSearch();    //START STATE
    }

    public void Update() {    //UPDATE ELEMENTS OF THE GUI
        density = (cells * cells) * dense;
        CSIZE = MSIZE / cells;
        canvas.repaint();
        cellsL.setText(cells + "x" + cells);
        msL.setText(delay + "ms");
        lengthL.setText("Path Length: " + length);
        densityL.setText(obstacles.getValue() + "%");
        checkL.setText("Checks: " + checks);

        //ACO
        alphaValueL.setText(String.valueOf(alpha));
        betaValueL.setText(String.valueOf(beta));
        rhoValueL.setText(String.valueOf(rho));
    }

    public void reset() {    //RESET METHOD
        solving = false;
        length = 0;
        checks = 0;
    }

    public void delay() {    //DELAY METHOD
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
        }
    }

    class Map extends JPanel implements MouseListener, MouseMotionListener {    //MAP CLASS

        public Map() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void paintComponent(Graphics g) {    //REPAINT
            super.paintComponent(g);
            for (int x = 0; x < cells; x++) {    //PAINT EACH NODE IN THE GRID
                for (int y = 0; y < cells; y++) {
                    switch (map[x][y].getType()) {
                        case 0:
                            g.setColor(Color.GREEN);
                            break;
                        case 1:
                            g.setColor(Color.RED);
                            break;
                        case 2:
                            g.setColor(Color.BLACK);
                            break;
                        case 3:
                            g.setColor(Color.WHITE);
                            break;
                        case 4:
                            g.setColor(Color.CYAN);
                            break;
                        case 5:
                            g.setColor(Color.YELLOW);
                            break;
                    }
                    if ((curAlg != 3 && curAlg != 4)
                            || (map[x][y].getType() != 3 && map[x][y].getType() != 4 && map[x][y].getType() != 5)) {
                        g.fillRect(x * CSIZE, y * CSIZE, CSIZE, CSIZE);
                    }

                    //绘制采样点及连线
                    if (curAlg == 3 || curAlg == 4) {

                    } else {
                        g.setColor(Color.BLACK);
                        g.drawRect(x * CSIZE, y * CSIZE, CSIZE, CSIZE);
                    }

                    /**RRT* paint*/
                    for (int i = 0; i < nodeList.size(); i++) {
                        //Node
                        g.setColor(Color.BLACK);
                        g.drawOval((int) ((nodeList.get(i).getX() - 0.1) * CSIZE), (int) ((nodeList.get(i).getY() - 0.1) * CSIZE), (int) (0.2 * CSIZE), (int) (0.2 * CSIZE));
                        //Line
                        g.setColor(Color.GREEN);
                        if (nodeList.get(i).getParent() != null)
                            g.drawLine((int) ((nodeList.get(i).getParent().getX()) * CSIZE), (int) ((nodeList.get(i).getParent().getY()) * CSIZE), (int) ((nodeList.get(i).getX()) * CSIZE), (int) ((nodeList.get(i).getY()) * CSIZE));
                    }

                    for (int i = 0; i < linePath.size(); i++) {
                        g.setColor(Color.RED);
                        if (linePath.get(i).getParent() != null)
                            g.drawLine((int) ((linePath.get(i).getParent().getX()) * CSIZE), (int) ((linePath.get(i).getParent().getY()) * CSIZE), (int) ((linePath.get(i).getX()) * CSIZE), (int) ((linePath.get(i).getY()) * CSIZE));
                    }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                int x = e.getX() / CSIZE;
                int y = e.getY() / CSIZE;
                Node current = map[x][y];
                if ((tool == 2 || tool == 3) && (current.getType() != 0 && current.getType() != 1))
                    current.setType(tool);
                Update();
            } catch (Exception z) {
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            resetMap();    //RESET THE MAP WHENEVER CLICKED
            try {
                int x = e.getX() / CSIZE;    //GET THE X AND Y OF THE MOUSE CLICK IN RELATION TO THE SIZE OF THE GRID
                int y = e.getY() / CSIZE;
                Node current = map[x][y];
                switch (tool) {
                    case 0: {    //START NODE
                        if (current.getType() != 2) {    //IF NOT WALL
                            if (startx > -1 && starty > -1) {    //IF START EXISTS SET IT TO EMPTY
                                map[startx][starty].setType(3);
                                map[startx][starty].setHops(-1);
                            }
                            current.setHops(0);
                            startx = x;    //SET THE START X AND Y
                            starty = y;
                            current.setType(0);    //SET THE NODE CLICKED TO BE START
                        }
                        break;
                    }
                    case 1: {//FINISH NODE
                        if (current.getType() != 2) {    //IF NOT WALL
                            if (finishx > -1 && finishy > -1)    //IF FINISH EXISTS SET IT TO EMPTY
                                map[finishx][finishy].setType(3);
                            finishx = x;    //SET THE FINISH X AND Y
                            finishy = y;
                            current.setType(1);    //SET THE NODE CLICKED TO BE FINISH
                        }
                        break;
                    }
                    default:
                        if (current.getType() != 0 && current.getType() != 1)
                            current.setType(tool);
                        break;
                }
                Update();
            } catch (Exception z) {
            }    //EXCEPTION HANDLER
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }
}
