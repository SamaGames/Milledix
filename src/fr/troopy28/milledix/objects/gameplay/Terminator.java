package fr.troopy28.milledix.objects.gameplay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.troopy28.milledix.Main;
import fr.troopy28.milledix.MilleDix;
import fr.troopy28.milledix.utils.ChatUtils;

public class Terminator {

	private MilleDix instance;
	
	/**
	 * @param instance Instance du jeu MilleDix.
	 */
	public Terminator(MilleDix instance){
		this.instance = instance;
	}
	
	/**
	 * Termine la partie. Définit l'état de jeu sur END_GAME, enlève 100 points au dernier joueur s'il n'a pas pu 
	 * placer sa forme (causeCannotPlace), affiche le nouveau scoreboard, donne l'inventaire d'attente à tous les
	 * joueurs, arrête toutes les tâches puis affiche le message adéquat de fin de partie en fonction des paramètres
	 * passés. <br>
	 * Nettoie ensuite le serveur en appelant la fonction cleanServer().
	 * @param p Joueur <u><i><b>PERDANT</b></i></u>. Il s'agit du joueur qui perdra 100 points si causeCannotPlace
	 * vaut true. 
	 * @param causeCannotPlace Booléen définissant si la fonction est appelée car il a été impossible pour le joueur
	 * perdant de placer sa forme. Mettre <b>true</b> si c'est le cas, sinon mettre <b>false</b>. Dans le cas où le
	 * booléen vaut <b>true</b>, le joueur passé en paramètre perdra 100 points.
	 */
	public void endGame(MDPlayer p, boolean causeCannotPlace){ //Recoit en paramètre le PERDANT SI causeCannotPlace = true
		Main.getInstance().setGameState(GameState.END_GAME);
		if(causeCannotPlace && p != null)
			p.decreaseScore(100); //On enlève 100 points au perdant	si il perd parce qu'il ne peut pas placer sa forme
		
		instance.getGameScoreboardManager().updateScoreboard(0, 0); //On affiche le nouveau scoreboard	avec les temps à zéro car la partie est terminée.

		for(String key : Main.getInstance().getGamePlayers().keySet()){
			Main.getInstance().get(key).setupWaitingInventory(); //On donne l'inventaire d'attente à tout le monde 
		}
		
		Bukkit.getScheduler().cancelTasks(Main.getInstance()); //On arrête toutes les tâches
		
		Bukkit.broadcastMessage(ChatColor.RED + "-----------------------------------------------------");
		Bukkit.broadcastMessage(ChatUtils.getGamePrefix() + ChatColor.GREEN + "La partie est terminée !");		
		
		//On récupère le gagnant. Vaut null en cas d'égalité
		MDPlayer winner = instance.getWinner(Main.getInstance().getGamePlayers().get(Main.getInstance().getGreenUUID()), Main.getInstance().getGamePlayers().get(Main.getInstance().getRedUUID())); 
		
		if(winner == null || p == null){ //Si il y a égalité
			Bukkit.broadcastMessage(ChatUtils.getGamePrefix() + ChatColor.AQUA + "Il y a égalité ! Félicitations à vous deux !");
		}else{
			Bukkit.broadcastMessage(ChatUtils.getGamePrefix() + ChatColor.AQUA + "Le joueur " + ChatColor.GOLD + ChatColor.BOLD + winner.getbPlayer().getName() + ChatColor.RESET +  ChatColor.AQUA + " a gagné la partie ! Félicitations !");
		}
		
		Bukkit.broadcastMessage(ChatColor.RED + "-----------------------------------------------------");
		rewardPlayers(winner);
		cleanServer();
	}
	
	/**
	 * Après 16 secondes d'attente, éjecte tous les joueurs connectés et redémarre le serveur.
	 */
	private void cleanServer(){
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable(){

			public void run() {
				for(Player pls : Bukkit.getOnlinePlayers()){
					pls.kickPlayer(ChatColor.GREEN + "La partie est terminée! Le plugin va procéder à un redémarrage!");
				}
				Bukkit.getPluginManager().disablePlugin(Main.getInstance());
				Bukkit.getPluginManager().enablePlugin(Main.getInstance());
			}
			
		}, 320);
	}
	
	/**
	 * Récompense les joueurs de la partie. Le gagnant obtient une étoile et 50 + (points_gagnant - points_perdant) coins.
	 * Si cette somme est supérieure à 100, il gagne 100 coins.
	 * Le perdant obtient 5 pièces + (score_perdant / 10).
	 * @param winner MDPlayer représentant le gagnant.
	 */
	private void rewardPlayers(MDPlayer winner){
		MDPlayer loser = null;
		for(String key : Main.getInstance().getGamePlayers().keySet()){ 
			if(Main.getInstance().get(key).getColor() != winner.getColor())
				loser = Main.getInstance().get(key);
		}
		winner.addCoins(getWinnerCoins(winner, loser), ChatColor.WHITE + "Vous avez gagné au " + ChatColor.AQUA + "MilleDix!");
		winner.addStars(1, "Vous avez gagné au " + ChatColor.AQUA + "MilleDix!");
		
		loser.addCoins(getLoserCoins(loser), ChatColor.WHITE +  "Vous avez joué au " + ChatColor.AQUA + "MilleDix!");
	}

	/**
	 * @param winner MDPlayer représentant le gagnant.
	 * @param loser MDPlayer représentant le perdant.
	 * @return Renvoie le nombre de points que doit gagner le gagant de la partie.
	 */
	private int getWinnerCoins(MDPlayer winner, MDPlayer loser){
		int result = 50 + winner.getScore() - loser.getScore();
		if(result > 100)
			result = 100;
		return result;
	}
	
	/**
	 * @param loser MDPlayer représentant le perdant.
	 * @return Renvoie le nombre de points que doit gagner le perdant de la partie.
	 */
	private int getLoserCoins(MDPlayer loser){
		int result = 5 + (loser.getScore() / 10);
		if(result < 5)
			result = 5;
		return result;
	}
	
	
}
