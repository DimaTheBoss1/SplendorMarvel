package splendormarvel.ia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import splendormarvel.Bot;
import splendormarvel.Jeu;
import splendormarvel.Joueur;
import splendormarvel.Lieu;
import splendormarvel.Personnage;
import splendormarvel.utils.Couleur;

/**
 * Ma stratégie de la fonction `jouer` suit ces étapes :
 * 1. Initialiser l'état du jeu à partir des données fournies.
 * 2. Priorité 1 : Réserver une carte stratégique si possible.
 * 3. Priorité 2 : Jouer une carte réservée ou acquérir une carte disponible en
 * fonction des synergies et priorités.
 * 4. Priorité 3 : Prendre des jetons en maximisant leur utilité pour les cartes
 * à acquérir.
 * 5. Vérifier si un lieu peut être pris en fonction des cartes possédées.
 * 6. Si un surplus de jetons est détecté (plus de 10), remettre les jetons les
 * moins importants.
 * 7. Retourner l'action optimale ou passer si aucune action n'est possible.
 */

public class Strat9814 extends Strat {
    int nbJ; // Le nombre de joueurs
    int place; // La place du joueur dans le tour
    int[] ordre; // L'ordre des joueurs
    int[][] jetonsJ; // Les jetons des joueurs
    int[][] reducs; // Les réductions des joueurs
    int[] points; // Les points des joueurs
    int[] avenger; // Le nombre d'Avengers des joueurs
    boolean rassemblement; // Le joueur possédant le rassemblement des Avengers
    int[] reserve; // Le nombre de cartes personnages qu' a réservés chaque joueur
    int[] nbreCartesRestantesDansPioche;
    int[] taillePioches; // Le nombre de cartes restantes dans les pioches
    int[] jetonsDispo; // Les jetons disponibles
    Lieu lieu1;
    int idLieu1;// Les lieux
    Lieu lieu2;
    int idLieu2;
    String nom; // Nom du joueur
    String[] action; // Action à effectuer
    int[] scores; // Les scores des joueurs (ajouté pour correspondre à afficher())
    Joueur joueurEnCours;
    int idJoueurEnCours; // Le joueur en cours d'action
    Joueur joueurSuivant;
    int idJoueurSuivant; // Le joueur suivant
    Joueur[] joueurs; // Liste des joueurs
    Jeu jeu;
    boolean positionAleatoire; // Position aléatoire pour le joueur humain

    /**
     * Constructeur de la classe Strat98
     */
    public Strat9814() {
        this.nom = "DIMA NKOA Gabriel 14";
    }

    /**
     * Retourne le nom du joueur.
     * 
     * @return Le nom du joueur.
     */
    @Override
    public String nomJoueur() {
        return nom;
    }

    /**
     * Initialise les données du jeu à partir de l'état initial.
     * 
     * @param etatDuJeu L'état initial du jeu sous forme de chaîne de caractères.
     */
    public void init(String etatDuJeu) {
        String[] lines = etatDuJeu.split("\\r?\\n"); // On split l'état du jeu par ligne
        this.nbJ = 0; // On récupère le nombre de joueurs
        // On initialise les tableaux dépendant du nombre de joueurs
        int[][] tableau = new int[5][5];
        String[] tableauString = { "lieu1", "lieu2" };
        this.lieu1 = new Lieu(tableauString, tableau);
        this.lieu2 = new Lieu(tableauString, tableau);
        this.joueurEnCours = new Bot(this, "jouerEnCours");
        this.joueurSuivant = new Bot(this, "jouerSuivant");
        this.joueurs = new Joueur[2];
        this.joueurs[0] = this.joueurEnCours;
        this.joueurs[0] = this.joueurSuivant;
        this.positionAleatoire = true; // Position aléatoire pour le joueur humain
        this.jeu = new Jeu(joueurs, positionAleatoire);
        this.nbreCartesRestantesDansPioche = new int[3];
        parserEtatDuJeu(lines);
    }

    /**
     * Met à jour les données du jeu à partir de l'état actuel.
     * 
     * @param etatDuJeu L'état actuel du jeu sous forme de tableau de chaînes de
     *                  caractères.
     */
    protected void parserEtatDuJeu(String[] lines) {
        nbJ = Integer.parseInt(lines[0]); // Nombre de joueurs
        place = Integer.parseInt(lines[1].split(" ")[0]); // Place du joueur actuel
        lieu1 = new Lieu(new String[2], new int[5][5]);
        lieu2 = new Lieu(new String[2], new int[5][5]);
        lieu1.disponible = true;
        lieu1.points = 3;
        lieu2.disponible = true;
        lieu2.points = 3;
        // Récupération des lieux
        for (int i = 0; i < 2; i++) {
            String[] lieuInfo = lines[18 + i].split(" ");

            int[] couleurs1 = {
                    Integer.parseInt(lieuInfo[1]), Integer.parseInt(lieuInfo[2]),
                    Integer.parseInt(lieuInfo[3]), Integer.parseInt(lieuInfo[4]),
                    Integer.parseInt(lieuInfo[5])
            };
            int[][] couleurs = { couleurs1, couleurs1 };

            // Lieux conquis et attribution couleurs nécessaires
            if (i == 0) {
                lieu1.conquis = Integer.parseInt(lieuInfo[6]);
                lieu1.couleurs = couleurs;
                idLieu1 = Integer.parseInt(lieuInfo[0]);
                lieu1.face = 1;
            } // On peut prendre la face qu'on veut O ou 1
            else {
                lieu2.conquis = Integer.parseInt(lieuInfo[6]);
                lieu2.couleurs = couleurs;
                idLieu2 = Integer.parseInt(lieuInfo[0]);
                lieu2.face = 1;
            } // On peut prendre la face qu'on veut O ou 1

        }

        // Récupération des informations des joueurs
        for (int i = 0; i < nbJ; i++) {
            if (i == place) {
                joueurEnCours = new Bot(this, "joueurEnCours");
                String[] joueurInfo = lines[1].split(" ");
                idJoueurEnCours = Integer.parseInt(joueurInfo[0]);// aussi égal à place

                // Points du joueur
                joueurEnCours.points = Integer.parseInt(joueurInfo[14]);

                // Nombre d'Avengers
                joueurEnCours.avenger = Integer.parseInt(joueurInfo[15]);

                // Jetons du joueur
                for (int j = 0; j < 7; j++) {
                    joueurEnCours.jetons[j] = Integer.parseInt(joueurInfo[6 + j]);
                }
                // Réductions du joueur
                for (int j = 0; j < 5; j++) {
                    joueurEnCours.reduc[j] = Integer.parseInt(joueurInfo[1 + j]);
                }
                // Cartes réservées (main)
                int nbCartesReservees = Integer.parseInt(joueurInfo[13]);
                for (int j = 0; j < nbCartesReservees; j++) {
                    // Ajout des cartes réservées (à partir des indices correspondants dans lines)
                    String[] carteResInfo = lines[21 + j].split(" ");
                    int cout[] = { Integer.parseInt(carteResInfo[2]), Integer.parseInt(carteResInfo[3]),
                            Integer.parseInt(carteResInfo[4]), Integer.parseInt(carteResInfo[5]),
                            Integer.parseInt(carteResInfo[6]) };

                    joueurEnCours.main.add(new Personnage(Integer.parseInt(carteResInfo[0]), "carte" + j, cout,
                            Integer.parseInt(carteResInfo[1]), Integer.parseInt(carteResInfo[8]),
                            Integer.parseInt(carteResInfo[7])));
                }
                // Rassemblement Avengers
                joueurEnCours.rassemblement = joueurInfo[16].equals("1");

            }

            if (i != place) {
                joueurSuivant = new Bot(this, "joueurSuivant");
                String[] joueurInfo = lines[2].split(" ");
                idJoueurSuivant = Integer.parseInt(joueurInfo[0]);// Aussi égal à i

                // Points du joueur
                joueurSuivant.points = Integer.parseInt(joueurInfo[14]);

                // Nombre d'Avengers
                joueurSuivant.avenger = Integer.parseInt(joueurInfo[15]);

                // Jetons du joueur
                for (int j = 0; j < 7; j++) {
                    joueurSuivant.jetons[j] = Integer.parseInt(joueurInfo[6 + j]);
                }

                // Réductions du joueur
                for (int j = 0; j < 5; j++) {
                    joueurSuivant.reduc[j] = Integer.parseInt(joueurInfo[1 + j]);
                }
                // Cartes réservées (main)
                int nbCartesReservees = Integer.parseInt(joueurInfo[13]);
                for (int j = 0; j < nbCartesReservees; j++) {
                    // Ajout des cartes réservées (à partir des indices correspondants dans lines)

                    String[] carteResInfo = lines[21 + j + Integer.parseInt(lines[1].split(" ")[13])].split(" ");
                    int cout[] = { Integer.parseInt(carteResInfo[2]), Integer.parseInt(carteResInfo[3]),
                            Integer.parseInt(carteResInfo[4]), Integer.parseInt(carteResInfo[5]),
                            Integer.parseInt(carteResInfo[6]) };

                    joueurSuivant.main.add(new Personnage(Integer.parseInt(carteResInfo[0]), "carte" + j, cout,
                            Integer.parseInt(carteResInfo[1]), Integer.parseInt(carteResInfo[8]),
                            Integer.parseInt(carteResInfo[7])));
                }
                // Rassemblement Avengers
                joueurSuivant.rassemblement = joueurInfo[16].equals("1");

            }
        }
        if (idJoueurEnCours == lieu1.conquis) {
            joueurEnCours.lieuxConquis.add(lieu1);
        } else if (idJoueurEnCours == lieu2.conquis) {
            joueurEnCours.lieuxConquis.add(lieu2);
        }
        if (idJoueurSuivant == lieu1.conquis) {
            joueurSuivant.lieuxConquis.add(lieu1);
        } else if (idJoueurSuivant == lieu2.conquis) {
            joueurSuivant.lieuxConquis.add(lieu2);
        }
        jeu.lieux[idLieu1] = lieu1;
        jeu.lieux[idLieu2] = lieu2;
        jeu.joueurs[idJoueurEnCours] = joueurEnCours;
        jeu.joueurs[idJoueurSuivant] = joueurSuivant;

        // Récupération des cartes disponibles
        for (int niveau = 0; niveau < 3; niveau++) {
            String[] piocheInfo = lines[3 + niveau * 5].split(" ");
            nbreCartesRestantesDansPioche[niveau] = Integer.parseInt(piocheInfo[0]);
            for (int i = 0; i < 4; i++) {
                String[] carteInfo = lines[4 + niveau * 5 + i].split(" ");
                if (carteInfo.length == 3) {// pas de carte dispo à cet emplacement
                    jeu.persos[niveau][Integer.parseInt(carteInfo[1])] = null;
                } else {// carte dispo à cet emplacement
                    int[] cout = {
                            Integer.parseInt(carteInfo[3]), Integer.parseInt(carteInfo[4]),
                            Integer.parseInt(carteInfo[5]), Integer.parseInt(carteInfo[6]),
                            Integer.parseInt(carteInfo[7])
                    };
                    jeu.persos[niveau][Integer.parseInt(carteInfo[1])] = new Personnage(niveau, "carte 1", cout,
                            Integer.parseInt(carteInfo[2]), Integer.parseInt(carteInfo[9]),
                            Integer.parseInt(carteInfo[8]));
                }

            }
        }

        // Récupération des jetons disponibles
        for (int i = 0; i < 7; i++) {
            jeu.jetons[i] = Integer.parseInt(lines[20].split(" ")[i]);
        }
        // System.out.println("Jeu bien parsé par joueur"+place);
    }

    @Override
    public String jouer(String etatDuJeu) {
        String[] lines = etatDuJeu.split("\\r?\\n");
        parserEtatDuJeu(lines);

        // Chercher les cartes que les deux joueurs peuvent prendre
        List<String[]> actions = new ArrayList<>();
        for (int niveau = 2; niveau >= 2; niveau--) { // Parcours par niveau décroissant
            for (int pos = 0; pos < 4; pos++) {
                if (jeu.persos[niveau][pos] != null) {
                    String[] actionJouerCarte = { "JOUER", String.valueOf(niveau), String.valueOf(pos) };
                    // Vérifier si les deux joueurs peuvent jouer cette carte
                    if (verifierJouerCarte(joueurEnCours, actionJouerCarte) &&
                            verifierJouerCarte(joueurSuivant, actionJouerCarte)) {
                        actions.add(actionJouerCarte);
                    }
                }
            }
        }

        // Si des cartes communes sont trouvées, jouer la première possible
        if (actions.size() > 0) {
            for (String[] actionJouerCarte : actions) {
                action = actionJouerCarte;
                String[] actionLieu = prendreLieu(joueurEnCours);
                if (actionLieu != null) {
                    return String.join(" ", actionJouerCarte) + "; " + String.join(" ", actionLieu);
                }
                return String.join(" ", actionJouerCarte);
            }
        }

        // recruter une carte
        String[][] cardPossibilities = {
                { "JOUER", "2" }, { "JOUER", "1" }, { "JOUER", "0" },
                { "JOUER", "2", "3" }, { "JOUER", "2", "2" }, { "JOUER", "2", "1" },
                { "JOUER", "2", "0" }, { "JOUER", "1", "3" }, { "JOUER", "1", "2" },
                { "JOUER", "1", "1" }, { "JOUER", "1", "0" }, { "JOUER", "0", "3" },
                { "JOUER", "0", "2" }, { "JOUER", "0", "1" }, { "JOUER", "0", "0" }
        };

        for (String[] actionJouerCarte : cardPossibilities) {
            if (verifierJouerCarte(joueurEnCours, actionJouerCarte)) {
                if (actionJouerCarte != null) {
                    action = actionJouerCarte;
                    String[] actionLieu = prendreLieu(joueurEnCours);
                    // //Prendre Lieu si possible
                    if (actionLieu != null) {
                        return String.join(" ", actionJouerCarte) + "; " + String.join(" ", actionLieu);
                    }
                    //
                    return String.join(" ", actionJouerCarte);
                }

            }

        }

        // Reservation carte
        String[][] reservePossibilities = {
                { "RESERVER", "2", "3" }, { "RESERVER", "2", "2" }, { "RESERVER", "2", "1" },
                { "RESERVER", "2", "0" }, { "RESERVER", "1", "3" }, { "RESERVER", "1", "2" },
                { "RESERVER", "1", "1" }, { "RESERVER", "1", "0" }, { "RESERVER", "0", "3" },
                { "RESERVER", "0", "2" }, { "RESERVER", "0", "1" }, { "RESERVER", "0", "0" }
        };
        for (String[] actionReserver : reservePossibilities) {
            if (verifierReserverCarte(actionReserver, joueurEnCours)) {
                if (actionReserver != null) {
                    action = actionReserver;
                    return String.join(" ", actionReserver);
                }

            }

        }

        // Enfin prendre jetons
        String[][] possibilities = {
                { "JETONS", "0", "1", "2" }, { "JETONS", "0", "1", "3" }, { "JETONS", "0", "1", "4" },
                { "JETONS", "0", "2", "3" }, { "JETONS", "0", "2", "4" }, { "JETONS", "0", "3", "4" },
                { "JETONS", "1", "2", "3" }, { "JETONS", "1", "2", "4" }, { "JETONS", "1", "3", "4" },
                { "JETONS", "2", "3", "4" }, { "JETONS", "0", "0" }, { "JETONS", "1", "1" },
                { "JETONS", "2", "2" }, { "JETONS", "3", "3" }, { "JETONS", "4", "4" },
                { "JETONS", "0", "1" }, { "JETONS", "0", "2" }, { "JETONS", "0", "3" }, { "JETONS", "0", "4" },
                { "JETONS", "1", "2" }, { "JETONS", "1", "3" }, { "JETONS", "1", "4" }, { "JETONS", "2", "3" },
                { "JETONS", "2", "4" }, { "JETONS", "3", "4" }, { "JETONS", "0" }, { "JETONS", "1" },
                { "JETONS", "2" }, { "JETONS", "3" }, { "JETONS", "4" }
        };

        for (String[] actionJouerJetons : possibilities) {
            if (verifierPrendreJeton(actionJouerJetons, joueurEnCours)) {
                if (actionJouerJetons != null) {
                    action = actionJouerJetons;
                    String[] actionRemettreJetons = remettreJetons(joueurEnCours);
                    // //Prendre Lieu si possible
                    if (actionRemettreJetons != null) {
                        return String.join(" ", actionJouerJetons) + "; " + String.join(" ", actionRemettreJetons);
                    }
                    //
                    return String.join(" ", actionJouerJetons);
                }

            }

        }

        // Si pas d'action'
        return "JETONS 0";
    }

    /**
     * Vérifie si le joueur peut prendre un lieu en fonction des cartes personnages
     * qu'il a recrutées.
     * 
     * @param joueur Le joueur actuel.
     * @param jeu    Le jeu contenant les lieux disponibles.
     * @return L'action du lieu à prendre, ou null si aucun lieu ne peut être pris.
     */
    public String[] prendreLieu(Joueur joueur) {
        String[] actionLieu = new String[2];
        int[] idLieux = { idLieu1, idLieu2 };
        actionLieu[0] = "LIEU";
        // Parcourt tous les lieux disponibles
        for (int idLieu : idLieux) {
            if (verifierLieu(idLieu, joueur)) {
                actionLieu[1] = String.valueOf(idLieu);
                return actionLieu;
            } // Fixe l'id du lieu voulu et retourne l'action;
        }
        // Aucun lieu ne peut être pris
        return null;
    }

    // Vérifie si le joueur pourra prendre un lieu
    public boolean verifierLieu(int id, Joueur joueur) {
        boolean res = false;
        // Checke que le lieu est bien disponible, qu'il n'est pas déjà conquis et qu'il
        // peut l'être par le joueur actif
        if (jeu.lieux[id].disponible && jeu.lieux[id].conquis < 0
                && jeu.joueurs[place] == joueur) {
            res = true;
        }
        return res && peutEtreConquis(id, joueur);
    }

    public boolean peutEtreConquis(int id, Joueur joueur) {
        // Vérifie si le joueur peut conquérir le lieu en fonction de ses réductions
        boolean res = true;
        for (int i = 0; i < 5; i++)
            if (joueur.reduc[i] < jeu.lieux[id].couleurs[0][i]) {
                res = false;
            }
        return res;
    }

    public boolean verifierJouerCarte(Joueur joueur, String[] action) {
        boolean res = true;
        int nb = action.length - 1;
        // Si un seul paramètre, checke si le joueur a bien la carte ciblée en main
        // Si 2 paramètres checke que la carte est bien disponible
        if ((nb == 1 && joueur.main.size() < Integer.parseInt(action[1]) + 1) ||
                (nb == 2 && (Integer.parseInt(action[1]) < 0 || Integer.parseInt(action[1]) > 3 ||
                        Integer.parseInt(action[2]) < 0 || Integer.parseInt(action[2]) > 3) &&
                        jeu.persos[Integer.parseInt(action[1])][Integer.parseInt(action[2])] == null)
                || jetonsDispoPourJouer(joueur, action) == false) {
            res = false;
        }
        return res;
    }

    /**
     * Methode copiée de verifier() dans PrendreJeton.java
     * Vérifie si le joueur peut prendre des jetons.
     * 
     * @param j      Le jeu actuel.
     * @param place  La place du joueur actif.
     * @param action L'action à vérifier.
     * @return true si l'action est valide, false sinon.
     */
    public boolean verifierPrendreJeton(String[] action, Joueur joueur) {
        int nb = action.length - 1;
        int[] jetonsVoulus = new int[nb];
        boolean res;
        for (int i = 0; i < nb; i++) {
            jetonsVoulus[i] = Integer.parseInt(action[i + 1]);
        }
        if (res = couleurValideEtDispo(action)) { // checke si les paramètres sont valides et que les couleurs
                                                  // correspondantes sont disponibles
            // Si c'est le cas on checke:
            // Dans le cas de 3 jetons qu'ils sont bien tous différents
            if (nb == 3 && (jetonsVoulus[0] == jetonsVoulus[1] || jetonsVoulus[0] == jetonsVoulus[2]
                    || jetonsVoulus[1] == jetonsVoulus[2]))
                res = false;
            // Dans le cas de 2 jetons identiques que le nombre de jetons dispos est bien
            // d'au moins 4
            else if (nb == 2 && jetonsVoulus[0] == jetonsVoulus[1] && jeu.jetons[jetonsVoulus[0]] < 4)
                res = false;
        }

        return res;
    }

    /**
     * Vérifie que les paramètres de l'action sont bien des couleurs valides et
     * disponibles
     * 
     * @param j le jeu dans son état actuel
     * @return vrai si les couleurs sont valides et disponibles, faux sinon
     */
    public boolean couleurValideEtDispo(String[] action) {
        // le cas pour la disponibilité de 2 jetons identiques est traité dans vérifier
        int nb = action.length - 1;
        int[] jetonsVoulus = new int[nb];
        boolean res = true;
        for (int i = 0; i < nb; i++) {
            jetonsVoulus[i] = Integer.parseInt(action[i + 1]);
        }
        for (int i = 0; i < nb; i++)
            if (jetonsVoulus[i] < 0 || jetonsVoulus[i] > 4 || jeu.jetons[jetonsVoulus[i]] < 1)
                res = false;
        return res;
    }

    public boolean verifierReserverCarte(String[] action, Joueur joueur) {
        boolean res = true;
        int[] id = new int[2];
        id[0] = Integer.parseInt(action[1]); // Niveau de la carte
        id[1] = Integer.parseInt(action[2]); // Index de la carte
        if (joueur.main.size() == 3 || id[0] < 0 || id[0] > 2
                || ((id[1] >= 0 && id[1] <= 3) && jeu.persos[id[0]][id[1]] == null)
                || ((id[1] < 0 || id[1] > 3) && nbreCartesRestantesDansPioche[id[0]] == 0)) {
            res = false;
        }
        return res;
    }

    public String[] remettreJetons(Joueur joueur) {
        String[] action = new String[4];
        action[0] = "REMETTRE";
        int totalPoints = 0;
        int[] getTokenDiffTotal = new int[5]; // Tableau pour stocker la somme des getTokenDiff pour chaque couleur

        // Étape 1 : Calculer getTokenDiffTotal
        for (int niveau = 0; niveau < jeu.persos.length; niveau++) {
            for (Personnage carte : jeu.persos[niveau]) {
                if (carte != null) {
                    totalPoints += carte.points; // Ajouter les points de la carte au total
                    for (int couleur = 0; couleur < 5; couleur++) {
                        getTokenDiffTotal[couleur] += getTokenDiff(carte, joueur, couleur) * carte.points;
                    }
                }
            }
        }

        // Étape 2 : Calculer l'importance des jetons sur le plateau
        ArrayList<Integer> importanceJetons = importanceJetonsOnBoard();

        // Étape 3 : Calculer la différence importance - getTokenDiffTotal / totalPoints
        double[] importanceRelative = new double[5];
        for (int couleur = 0; couleur < 5; couleur++) {
            importanceRelative[couleur] = importanceJetons.get(couleur)
                    - (getTokenDiffTotal[couleur] / (double) totalPoints);
        }

        // Étape 4 : Classer les couleurs par importance croissante
        List<Integer> couleursTriees = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            couleursTriees.add(i);
        }
        couleursTriees.sort((a, b) -> Double.compare(importanceRelative[a], importanceRelative[b]));

        // Étape 5 : Remettre les jetons les moins importants
        int totalJetons = Arrays.stream(joueur.jetons).sum(); // Calculer le total des jetons du joueur
        for (int couleur : couleursTriees) {
            while (joueur.jetons[couleur] > 0 && totalJetons > 10) {
                joueur.jetons[couleur]--; // Remettre un jeton de cette couleur
                totalJetons--;
                action[action.length] = String.valueOf(couleur); // Ajouter l'action de remise de jeton
            }
        }
        if (verifierRemettreJeton(action, joueur)) {
            return action; // Retourner l'action de remise de jeton
        } else {
            return null;
        }
    }

    public boolean verifierRemettreJeton(String[] action, Joueur joueur) {
        if (Arrays.stream(joueurs[place].jetons).sum() <= 10) {
            return false;// On ne peut pas remettre de jetons si le joueur en a moins de 10
        }
        int[] jetonsRemis = new int[3];
        int[] cpt = new int[6];
        int nb = action.length - 1; // On récupère le nombre de paramètres
        for (int i = 0; i < nb; i++) {
            jetonsRemis[i] = Integer.parseInt(action[i + 1]);// On fixe les jetons à remettre
        }
        boolean res = true;
        for (int i = 0; i < 6; i++)
            cpt[i] = 0; // On réinitialise le tableau pour compter les jetons
        for (int i = 0; i < nb; i++) {
            // Pour chaque paramètre on vérifie que c'est une couleur valide (compris dans
            // [0,5], on ne peut pas remettre un jeton vert)
            if (jetonsRemis[i] < 0 || jetonsRemis[i] > 5)
                res = false;
            else
                cpt[jetonsRemis[i]]++; // si c'est le cas on enregistre l'info
        }
        if (res) { // Pour chaque couleur on vérifie que le joueur a bien les jetons voulus
            for (int i = 0; i < 6; i++) {
                if (joueur.jetons[i] < cpt[i])
                    res = false;
            }
        }
        return res;
    }

    /**
     * Cette méthode vérifie le nombre de jetons disponibles sur le plateau de jeu
     * et ajuste les scores des jetons en conséquence.
     * 
     * @param j          Le jeu actuel.
     * @param tokenScore La liste des scores des jetons.
     * @return La liste mise à jour des scores des jetons.
     */

    private ArrayList<Integer> importanceJetonsOnBoard() {
        // Initialise un tableau de scores pour chaque type de jeton (disponible dans le
        // jeu)
        ArrayList<Integer> tokenScore = new ArrayList<>();
        for (int i = 0; i < jeu.jetons.length; i++) {
            tokenScore.add(0); // Initialise chaque score à 0
        }

        // Parcourt chaque type de jeton (indices 0 à 4 pour les couleurs, on compte pas
        // les jokers pour les jokers ni les jetons verts)
        for (int i = 0; i < jeu.jetons.length - 2; i++) {
            int numOfTokenOnBoard = jeu.jetons[i]; // Nombre de jetons disponibles pour ce type

            // Ajuste le score du jeton en fonction de sa quantité disponible
            switch (numOfTokenOnBoard) {
                case 0: // Si aucun jeton de ce type n'est disponible
                    tokenScore.set(i, -1); // Marque ce jeton comme indisponible
                    break;
                case 1: // Si seulement 1 jeton est disponible
                    tokenScore.set(i, 4); // Augmente fortement son score
                    break;
                case 2: // Si 2 jetons sont disponibles
                    tokenScore.set(i, 3); // Augmente modérément son score
                    break;
                case 3: // Si 3 jetons sont disponibles
                    tokenScore.set(i, 2); // Augmente légèrement son score
                    break;
                case 4: // Si 4 jetons sont disponibles
                    tokenScore.set(i, 1); // Augmente faiblement son score
                    break;
                default:
                    break; // Ne fait rien pour les autres cas
            }
        }
        // Retourne la liste des scores mise à jour
        return tokenScore;
    }

    /**
     * Cette méthode calcule la différence entre le coût en jetons d'une carte et le
     * nombre de jetons et réduc possédés par un joueur.
     * 
     * @param carte     La carte à analyser.
     * @param joueur    Le joueur pour lequel on effectue l'analyse.
     * @param jetonType Le type de jeton à analyser (0 à 4 pour les couleurs, 5 pour
     *                  les jokers, 6 pour les jetons verts).
     * @return La différence entre le coût en jetons de la carte et le nombre de
     *         jetons possédés par le joueur.
     */
    private int getTokenDiff(Personnage carte, Joueur joueur, int jetonType) {
        // Get the number of tokens required for the card of the specified type
        int requiredTokens = carte.cout[jetonType];

        // Get the number of tokens the player owns of the specified type
        int ownedTokens = joueur.jetons[jetonType];

        // Get the number of reductions the player has for the specified type
        int reductions = joueur.reduc[jetonType];

        // Calculate and return the difference
        return requiredTokens - ownedTokens - reductions;
    }

    public boolean jetonsDispoPourJouer(Joueur joueur, String[] action) {
        boolean res = true;
        int joker = joueur.jetons[5]; // le nombre de jokers du joueur
        int cpt = 0;
        int nb = action.length - 1;
        Personnage perso;
        int[] jetonsRequis = new int[5]; // le nombre de jetons requis pour la carte
        // On fixe la carte voulue
        if (nb == 1)
            perso = joueur.main.get(Integer.parseInt(action[1])); // carte en main
        else if (nb == 2 && jeu.persos[Integer.parseInt(action[1])][Integer.parseInt(action[2])] == null) {
            return false;
        } else
            perso = jeu.persos[Integer.parseInt(action[1])][Integer.parseInt(action[2])]; // carte en jeu
        for (int i = 0; i < 5; i++) // on définit le nombre de jetons requis en fonction des réductions du joueur
            jetonsRequis[i] = Math.max(0, perso.cout[i] - joueur.reduc[i]);
        while (cpt < 5 && res && joker >= 0) { // On compte le nombre de jokers nécessaires (quand le joueur n'a pas
                                               // assez de jetons d'une couleur)
            if (jetonsRequis[cpt] > (joueur.jetons[cpt] + joker))
                res = false; // Si le joueur n'as pas assez de jokers, on fixe la valeur de retour à faux
            else
                joker -= (Math.max(0, jetonsRequis[cpt] - joueur.jetons[cpt])); // Sinon on décrémente le nombre de
                                                                                // jokers
            cpt++; // Et on passe à la couleur suivante
        }
        return res;
    }

    public String[] combinerAvecPriseLieu(String[] actionJouer, Joueur joueur) {
        if (actionJouer == null) {
            return null;
        }
        // On simule le jeu de la carte
        Joueur joueurSimule = simulerJouerCarte(actionJouer, joueur);

        // On vérifie si on peut prendre un lieu avec l'état simulé
        String[] actionLieu = prendreLieu(joueurSimule);

        // Si pas de lieu à prendre
        if (actionLieu == null) {
            return actionJouer;
        }

        // On combine les deux actions
        StringBuilder actionComplete = new StringBuilder();
        for (String part : actionJouer) {
            actionComplete.append(part).append(" ");
        }

        actionComplete.append(";");

        for (String part : actionLieu) {
            actionComplete.append(" ").append(part);
        }

        return actionComplete.toString().trim().split(" ");
    }

    private Joueur simulerJouerCarte(String[] actionJouer, Joueur joueur) {
        Joueur joueurSimule = joueur;
        // Si on joue une carte de la main, on l'ajoute aux reducs du joueur
        if (actionJouer.length == 2) {
            int indexMain = Integer.parseInt(actionJouer[1]);
            if (indexMain >= 0 && indexMain < joueur.main.size()) {
                joueurSimule.reduc[joueurSimule.main.get(indexMain).couleur] += 1;
            }
        }
        // Si on joue une carte du plateau, on l'ajoute aux reducs du joueur
        else if (actionJouer.length == 3) {
            int niveau = Integer.parseInt(actionJouer[1]);
            int index = Integer.parseInt(actionJouer[2]);
            if (niveau >= 0 && niveau < jeu.persos.length &&
                    index >= 0 && index < jeu.persos[niveau].length &&
                    jeu.persos[niveau][index] != null) {
                joueurSimule.reduc[jeu.persos[niveau][index].couleur] += 1;
            }
        }

        return joueurSimule;
    }

}
