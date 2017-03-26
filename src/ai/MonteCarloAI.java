package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import othello.Move;
import othello.Othello;
import othello.Player;
import othello.State;
import utils.Timeout;

/**
 *
 * @author Vincent
 */
public class MonteCarloAI extends AbstractAI {
    public static final String NAME = "montecarlo";
    
    GameTree tree;
    private Timeout timer;
    private GameTreeNode current;
    private int explorationCount;
    private int defaultTimeout;
    private Player player = Player.NullPlayer;
    
    public MonteCarloAI(String arg_string) {
        super(arg_string);
        
        tree = new GameTree(new Othello());
        current = tree.getRoot();
        current.total_branches = current.childs.length;
        
        explorationCount = 100;
        defaultTimeout = 750;
        
        if(arg_string != null && !arg_string.isEmpty()) {
            String[] args = arg_string.split(",");
            for(String a : args) {
                if(a.startsWith("num=")) {
                    try {
                        explorationCount = Integer.parseInt(a.substring("num=".length()));
                    } catch(Exception e) {
                        System.err.println("[montecarlo] Invalid format for 'num' argument.");
                    }
                } else if(a.startsWith("time=")) {
                    try {
                        defaultTimeout = Integer.parseInt(a.substring("time=".length()));
                    } catch(Exception e) {
                        System.err.println("[montecarlo] Invalid format for 'time' argument.");
                    }
                } else {
                    System.err.println("[montecarlo] Unknown option '" + a + "'.");
                }
            }
        }
        
        if(explorationCount <= 0) {
            explorationCount = -1;
        }
        
        if(defaultTimeout <= 0) {
            defaultTimeout = -1;
        }
        
        if(explorationCount <= 0 && defaultTimeout <= 0) {
            explorationCount = 64;
            defaultTimeout = 750;
        }
        
        timer = new Timeout();
    }
    
    public void setExplorationCount(int n) {
        explorationCount = n;
    }

    @Override
    public void notifyPass()
    {
        // nothing to do
    }
    

    @Override
    public void notifyRewind(int n)
    {
        while(n > 0) {
            current = current.parent;
            n -= 1;
        }
    }
    
    @Override
    public void notifyMove(Move m)
    {
        int index = current.moves.indexOf(m);
        if(index < 0) {
            System.err.println("[Warning] MonteCarloAI.notifyMove() failed");
            return;
        }
        Othello game = new Othello(current.getGame());
        game.makeMove(m);
        while(isInPassState(game)) {
            game.acknowledgePass(activePlayer(game));
        }
        if(current.childs[index] == null) {
            current.childs[index] = new GameTreeNode(tree, current, index, game.possibleMoves());
            propagateUnexploredBranchesCount(current, current.childs[index].total_branches - 1);
        }
        current = current.childs[index];
        current.game = game;
    }
    
    @Override
    public void notifyLoad(Othello game)
    {
        tree = new GameTree(new Othello(game));
        current = tree.getRoot();
        current.total_branches = current.childs.length;
    }
    
    @Override
    public Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout)
    {
        if (timeout > 0) {
            this.timer.start(Math.min(timeout, this.defaultTimeout));
        } else {
            this.timer.start(this.defaultTimeout);
        }
        

        
        if(this.player == Player.NullPlayer) {
            if(game.getState() == State.WhitePlayerTurn) {
                this.player = Player.WhitePlayer;
            } else {
                this.player = Player.BlackPlayer;
            }
        }
        
        
        Othello mygame = new Othello(game);
        int explored = 0;
        while((explorationCount == -1 || explored < explorationCount) && !this.timer.expired()) {
            if(!explore(current, mygame)) {
                break;
            }
            explored += 1;
        }
        
        long elapsed = this.timer.elapsed();
        
        int select = selectBranchToPlay(current);
        Move result = current.moves.get(select);
        
        print("[" + MonteCarloAI.NAME + "] Explored " + explored + " branches in " + elapsed + "ms");
        print("[" + MonteCarloAI.NAME  + "] Selected " + game.posString(result.getPos()) + " branch");
        GameTreeNode selected_branch = current.childs[select];
        print("[" + MonteCarloAI.NAME  + "] This branch has an estimated winning probability of " + ((double) selected_branch.winning_branches / selected_branch.explored_branches));
        print("[" + MonteCarloAI.NAME  + "] " + selected_branch.explored_branches +  " branches have been explored out of at least " + selected_branch.total_branches);
        print("[" + MonteCarloAI.NAME  + "] Exploration rate is less than " + ( (double)selected_branch.explored_branches / selected_branch.total_branches));
        
        return result;
    }
    
    public boolean explore(GameTreeNode node, Othello game) {
        if(game.getState() == State.GameOver) {
            boolean win = (game.getWinner() == this.player);
            propagateExplorationResult(node.parent, win);
            return false;
        }
        
        if(node.explored_branches == node.total_branches) {
            // node already fully explored
            return false;
        }

        int which = selectBranchToExplore(node, game);
        
        GameTreeNode branch = node.childs[which];
        game.makeMove(node.moves.get(which));
        while (isInPassState(game)) {
            game.acknowledgePass(activePlayer(game));
        }
        explore(branch, game);
        game.popLastMove();
        return true;
    }
    
    public int selectBranchToPlay(GameTreeNode node)
    {
        int selected = 0;
        double win_rate = 0.0;
        for(int i = 0; i < node.childs.length; ++i) {
            if(node.childs[i] == null) {
                continue;
            }
            double mywinrate = node.childs[i].winning_branches / (double) node.childs[i].explored_branches;
            if(mywinrate > win_rate) {
                selected = i;
                win_rate = mywinrate;
            } else if(mywinrate == win_rate) {
                Random rgen = new Random();
                if(rgen.nextBoolean()) {
                    selected = i;
                    win_rate = mywinrate;
                }
            }
        }
        return selected;
    }
    
    public int selectBranchToExplore(GameTreeNode node, Othello game)
    {
        List<GameTreeNode> unexplored_branches = new ArrayList<GameTreeNode>();
        int total_plays = 0; // the total numbers of simulation done
        for(int i = 0; i < node.childs.length; ++i) {
            if(node.childs[i] == null) {
                game.makeMove(node.moves.get(i));
                while(isInPassState(game)) {
                    game.acknowledgePass(activePlayer(game));
                }
                node.childs[i] = new GameTreeNode(tree, node, i, game.possibleMoves());
                if(node.childs[i].total_branches > 0) {
                    propagateUnexploredBranchesCount(node, node.childs[i].total_branches - 1);
                }
                game.popLastMove();
            }
            
            if(node.childs[i].explored_branches == 0) {
                unexplored_branches.add(node.childs[i]);
            }
            
            total_plays += node.childs[i].explored_branches;
        }
        
        if(unexplored_branches.size() > 0) {
            // at least one branch has not been explored yet
            // we select on of those randomly
            Random gen = new Random();
            int which = gen.nextInt(unexplored_branches.size());
            return unexplored_branches.get(which).parent_index;
        }
       
        // all branches have been explored at least once, we can apply a 
        // selection algorithm to decide which branch to choose
        int selected = 0;
        double ucb1 = UCB1(node.childs[0], total_plays);
        for(int i = 1; i < node.childs.length; ++i) {
            if(node.childs[i].explored_branches == node.childs[i].total_branches) {
                // the node has already been fully explored, 
                // it does not qualify for exploration
                continue;
            }
            double node_ucb1 = UCB1(node.childs[i], total_plays);
            if(node_ucb1  > ucb1) {
                selected = i;
                ucb1 = node_ucb1;
            }
        }
        
        return selected;
    }
    
    public double UCB1(GameTreeNode n, int total_plays) {
        final double exploration_factor = Math.sqrt(2);

        double winning_rate = n.winning_branches / (double) n.explored_branches;
        double exploration_component = Math.sqrt(Math.log(total_plays) / (double) n.childs[0].explored_branches);
        double ucb1 = winning_rate + exploration_factor * exploration_component;
        return ucb1;
    }
    
    public void propagateUnexploredBranchesCount(GameTreeNode node, int diff) {
        if(node == null) {
            return;
        }
        
        node.total_branches += diff;
        propagateUnexploredBranchesCount(node.parent, diff);
    }
    
    public void propagateExplorationResult(GameTreeNode node, boolean win) {
        if(node == null) {
            return;
        }
        
        node.explored_branches += 1;
        node.winning_branches += (win ? 1 : 0);
        propagateExplorationResult(node.parent, win);
    }
    
    public boolean isInPassState(Othello game) {
        return game.getState() == State.WhitePlayerPass 
                || game.getState() == State.BlackPlayerPass;
    }
    
    public Player activePlayer(Othello game) {
        if(game.getState() == State.WhitePlayerPass
                || game.getState() == State.WhitePlayerTurn) {
            return Player.WhitePlayer;
        } else if(game.getState() == State.GameOver) {
            return Player.NullPlayer;
        } 
        return Player.BlackPlayer;
    }
}
