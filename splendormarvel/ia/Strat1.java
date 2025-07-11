package splendormarvel.ia;

import java.util.*;

/**
 * Stratégie simple
 * Priorité : 1) Réserver cartes niveau 3 si adversaire peut les acheter
 *           2) Acheter cartes disponibles (niveau 3->2->1)
 *           3) Prendre jetons selon fonction d'évaluation
 *           4) Gérer limite de 10 jetons
 *           5) Gérer les cas exceptionnels
 */
public class Strat1 extends Strat {

    // Données du jeu parsées
    private int nbJoueurs;
    private CustomJoueur joueurCourant;
    private CustomJoueur adversaire;
    private int[] jetonsDisponibles; // [jaune, bleu, orange, violet, rouge, gris, vert]
    private List<CustomCarte> cartesNiveau1;
    private List<CustomCarte> cartesNiveau2;
    private List<CustomCarte> cartesNiveau3;
    private List<CustomCarte> cartesReservees;
    private int nbCartesReservees;

    // Classe interne pour représenter un joueur
    private class CustomJoueur {
        int position;
        int[] reductions = new int[5]; // [jaune, bleu, orange, violet, rouge]
        int[] jetons = new int[7]; // [jaune, bleu, orange, violet, rouge, gris, vert]
        int nbCartesReservees;
        int points;
        int avengers;
        boolean aRassemblement;
    }

    // Classe interne pour représenter une carte
    private class CustomCarte {
        int niveau;
        int position;
        int couleur;
        int[] cout = new int[5]; // [jaune, bleu, orange, violet, rouge]
        int points;
        int avengers;
        boolean existe;
    }

    @Override
    public String nomJoueur() {
        return "JANNY Lou";
    }

    @Override
    public void init(String etatDuJeu) {
        // Initialisation au début de chaque partie
        cartesNiveau1 = new ArrayList<>();
        cartesNiveau2 = new ArrayList<>();
        cartesNiveau3 = new ArrayList<>();
        cartesReservees = new ArrayList<>();
    }

    @Override
    public String jouer(String etatDuJeu) {
        // Parser l'état du jeu
        parseEtatDuJeu(etatDuJeu);

        // 1) Vérifier si adversaire peut acheter carte niveau 3
        for (CustomCarte carte : cartesNiveau3) {
            if (carte.existe && carteAchetable(carte, adversaire) &&
                    !carteAchetable(carte, joueurCourant) && nbCartesReservees < 3) {
                return "RESERVER 2 " + carte.position;
            }
        }

        // 2) Acheter une carte si possible (priorité niveau 3->2->1)
        String achat = chercherCarteAchetable();
        if (achat != null) {
            return achat;
        }

        // 3) Prendre des jetons selon évaluation
        String priseJetons = prendreJetonsOptimaux();
        if (priseJetons != null) {
            return priseJetons;
        }

        // 4) Si aucun jeton disponible, remettre un jeton au hasard
        return remettreJetonAleatoire();
    }

    private void parseEtatDuJeu(String etat) {
        String[] lignes = etat.split("\n");
        int ligneIndex = 0;

        // Nombre de joueurs
        nbJoueurs = Integer.parseInt(lignes[ligneIndex++]);

        // Infos joueur courant
        joueurCourant = parseJoueur(lignes[ligneIndex++]);

        // Infos adversaire
        adversaire = parseJoueur(lignes[ligneIndex++]);

        // Cartes niveau 1
        int nbCartesRestantes1 = Integer.parseInt(lignes[ligneIndex++]);
        cartesNiveau1.clear();
        for (int i = 0; i < 4; i++) {
            cartesNiveau1.add(parseCarte(lignes[ligneIndex++], 0));
        }

        // Cartes niveau 2
        int nbCartesRestantes2 = Integer.parseInt(lignes[ligneIndex++]);
        cartesNiveau2.clear();
        for (int i = 0; i < 4; i++) {
            cartesNiveau2.add(parseCarte(lignes[ligneIndex++], 1));
        }

        // Cartes niveau 3
        int nbCartesRestantes3 = Integer.parseInt(lignes[ligneIndex++]);
        cartesNiveau3.clear();
        for (int i = 0; i < 4; i++) {
            cartesNiveau3.add(parseCarte(lignes[ligneIndex++], 2));
        }

        // Lieux (on les ignore pour l'instant)
        for (int i = 0; i < nbJoueurs; i++) {
            ligneIndex++; // Sauter les lignes de lieux
        }

        // Jetons disponibles
        String[] jetonsTokens = lignes[ligneIndex++].split(" ");
        jetonsDisponibles = new int[7];
        for (int i = 0; i < 7; i++) {
            jetonsDisponibles[i] = Integer.parseInt(jetonsTokens[i]);
        }

        // Cartes réservées
        nbCartesReservees = joueurCourant.nbCartesReservees;
        cartesReservees.clear();

        // Total des cartes réservées de tous les joueurs
        int totalCartesReservees = 0;
        for (int i = 0; i < nbJoueurs; i++) {
            // Pour chaque joueur (en commençant par le joueur courant)
            int nbCartesJoueur = (i == 0) ? joueurCourant.nbCartesReservees : adversaire.nbCartesReservees;

            for (int j = 0; j < nbCartesJoueur; j++) {
                if (ligneIndex < lignes.length) {
                    String[] carteTokens = lignes[ligneIndex++].split(" ");
                    if (i == 0) { // Seulement les cartes du joueur courant
                        CustomCarte carte = new CustomCarte();
                        carte.niveau = Integer.parseInt(carteTokens[0]) - 1;
                        carte.couleur = Integer.parseInt(carteTokens[1]);
                        for (int k = 0; k < 5; k++) {
                            carte.cout[k] = Integer.parseInt(carteTokens[2 + k]);
                        }
                        carte.points = Integer.parseInt(carteTokens[7]);
                        carte.avengers = Integer.parseInt(carteTokens[8]);
                        carte.existe = true;
                        cartesReservees.add(carte);
                    }
                }
            }
        }
    }

    private CustomJoueur parseJoueur(String ligne) {
        String[] tokens = ligne.split(" ");
        CustomJoueur j = new CustomJoueur();
        j.position = Integer.parseInt(tokens[0]);

        // Réductions (5 couleurs)
        for (int i = 0; i < 5; i++) {
            j.reductions[i] = Integer.parseInt(tokens[1 + i]);
        }

        // Jetons (7 types)
        for (int i = 0; i < 7; i++) {
            j.jetons[i] = Integer.parseInt(tokens[6 + i]);
        }

        j.nbCartesReservees = Integer.parseInt(tokens[13]);
        j.points = Integer.parseInt(tokens[14]);
        j.avengers = Integer.parseInt(tokens[15]);
        j.aRassemblement = Integer.parseInt(tokens[16]) == 1;

        return j;
    }

    private CustomCarte parseCarte(String ligne, int niveau) {
        String[] tokens = ligne.split(" ");
        CustomCarte c = new CustomCarte();
        c.niveau = Integer.parseInt(tokens[0]);
        c.position = Integer.parseInt(tokens[1]);
        c.couleur = Integer.parseInt(tokens[2]);

        if (c.couleur == -1) {
            c.existe = false;
            return c;
        }

        c.existe = true;
        for (int i = 0; i < 5; i++) {
            c.cout[i] = Integer.parseInt(tokens[3 + i]);
        }
        c.points = Integer.parseInt(tokens[8]);
        c.avengers = Integer.parseInt(tokens[9]);
        return c;
    }

    // Vérifie si une carte est envisageable
    private boolean carteEnvisageable(CustomCarte carte, CustomJoueur joueur) {
        if (!carte.existe) return false;

        int somme = 0;
        for (int i = 0; i < 5; i++) {
            int diff = carte.cout[i] - joueur.reductions[i];
            if (diff > 4) return false; // Plus de 4 jetons d'une couleur
            if (diff > 0) somme += diff;
        }
        return somme <= 9; // Maximum 9-10 jetons
    }

    // Vérifie si une carte est achetable ce tour
    private boolean carteAchetable(CustomCarte carte, CustomJoueur joueur) {
        if (!carte.existe) return false;

        int manquants = 0;
        for (int i = 0; i < 5; i++) {
            int diff = carte.cout[i] - joueur.reductions[i] - joueur.jetons[i];
            if (diff > 0) manquants += diff;
        }
        return manquants <= joueur.jetons[5]; // Assez de jokers
    }

    // Évalue l'intérêt de chaque couleur de jeton
    private int[] evaluerJetons() {
        int[] valeurs = new int[5];

        // Évaluer toutes les cartes
        List<List<CustomCarte>> toutesCartes = Arrays.asList(cartesNiveau1, cartesNiveau2, cartesNiveau3);
        for (List<CustomCarte> niveau : toutesCartes) {
            for (CustomCarte carte : niveau) {
                if (carteEnvisageable(carte, joueurCourant)) {
                    for (int couleur = 0; couleur < 5; couleur++) {
                        int diff = carte.cout[couleur] - joueurCourant.reductions[couleur] - joueurCourant.jetons[couleur];
                        if (diff > 0) {
                            valeurs[couleur]++;
                        }
                    }
                }
            }
        }

        return valeurs;
    }

    // Cherche une carte achetable (priorité niveau 3->2->1)
    private String chercherCarteAchetable() {
        // Vérifier cartes niveau 3
        for (CustomCarte carte : cartesNiveau3) {
            if (carteAchetable(carte, joueurCourant)) {
                return "JOUER 2 " + carte.position;
            }
        }

        // Vérifier cartes niveau 2
        for (CustomCarte carte : cartesNiveau2) {
            if (carteAchetable(carte, joueurCourant)) {
                return "JOUER 1 " + carte.position;
            }
        }

        // Vérifier cartes niveau 1
        for (CustomCarte carte : cartesNiveau1) {
            if (carteAchetable(carte, joueurCourant)) {
                return "JOUER 0 " + carte.position;
            }
        }

        // Vérifier cartes réservées
        for (int i = 0; i < cartesReservees.size(); i++) {
            if (carteAchetable(cartesReservees.get(i), joueurCourant)) {
                return "JOUER " + i;
            }
        }

        return null;
    }

    // Prend les jetons optimaux selon évaluation
    private String prendreJetonsOptimaux() {
        int[] valeurs = evaluerJetons();

        // Créer liste triée des couleurs par valeur décroissante
        List<Integer> couleurs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            couleurs.add(i);
        }
        couleurs.sort((a, b) -> valeurs[b] - valeurs[a]);

        // Essayer de prendre 3 jetons différents
        List<Integer> aPrend = new ArrayList<>();
        for (int couleur : couleurs) {
            if (jetonsDisponibles[couleur] > 0 && aPrend.size() < 3) {
                aPrend.add(couleur);
            }
        }

        if (aPrend.size() >= 1) {
            // Si on peut prendre 2 jetons de la même couleur
            if (aPrend.size() == 1 && jetonsDisponibles[aPrend.get(0)] >= 4) {
                return "JETONS " + aPrend.get(0) + " " + aPrend.get(0);
            }

            // Sinon prendre les jetons différents
            StringBuilder sb = new StringBuilder("JETONS");
            for (int c : aPrend) {
                sb.append(" ").append(c);
            }

            // Gérer limite de 10 jetons
            int totalJetons = 0;
            for (int i = 0; i < 7; i++) {
                totalJetons += joueurCourant.jetons[i];
            }

            if (totalJetons + aPrend.size() > 10) {
                sb.append(" ; REMETTRE");
                int aRemettre = totalJetons + aPrend.size() - 10;
                for (int i = 0; i < 5 && aRemettre > 0; i++) {
                    if (joueurCourant.jetons[i] > 0 && valeurs[i] == 0) {
                        sb.append(" ").append(i);
                        aRemettre--;
                    }
                }
            }

            String str = sb.toString();

            return str.endsWith(" ; REMETTRE") ? str.substring(0, str.length() - 11) : str;
        }

        return null;
    }

    // Remet un jeton aléatoire si nécessaire
    private String remettreJetonAleatoire() {
        for (int i = 0; i < 5; i++) {
            if (joueurCourant.jetons[i] > 0) {
                return "REMETTRE " + i;
            }
        }
        return "JETONS"; // Action par défaut
    }
}