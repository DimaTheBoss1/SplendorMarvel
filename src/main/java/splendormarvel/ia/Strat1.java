package splendormarvel.ia;

import java.util.ArrayList;
import java.util.List;
/**
 * Cette stratégie vise à s’adapter à chaque tour en fonction de l’état du jeu.
 * Elle commence par analyser si une victoire est possible rapidement.  
 * Si l’achat d’une carte importante est envisageable, elle le fait en priorité.
 * Lorsqu’aucune bonne option n’est disponible, elle réserve une carte utile.
 * Elle choisit en priorité les cartes de haut niveau si la victoire est proche,
 * sinon elle se concentre sur les cartes plus accessibles pour progresser.
 * Elle prend les jetons les plus utiles pour ses prochains achats.
 * Elle évite de bloquer en gérant bien le nombre de jetons en main.
 * Si l’adversaire est menaçant, elle peut aussi bloquer ses cartes.
 * Enfin, elle évite de passer son tour en trouvant toujours une action pertinente.
 */

public class Strat1 extends Strat {

    private int[] reducCourante = new int[5];
    private int[] jetonsCourants = new int[7];
    private int nbJetons, nbPtVictoire, nbAvengers;
    private boolean rassemblementAvengers;
    
    private int[] reducSuivante = new int[5];
    private int[] jetonsSuivants = new int[7];
    private int nbPtVictoire2;
    
    private int nbTour = 0;
    private String etatDuJeu;
    private int dernierTourAcheter = 0;
    private int nbCartesReservees = 0;

    @Override
    public String nomJoueur() {
        return "LE ROUX--TARDIF brieuc";
    }
    
    @Override
    public String jouer(String etatDuJeu) {
        if (nbTour == 0 || (sommeTableau(jetonsCourants, 5) == 0 && sommeTableau(reducCourante, 5) == 0)) {
            init(etatDuJeu);
        }
        
        this.etatDuJeu = etatDuJeu;
        nbTour++;
        
        parseEtatJeu(etatDuJeu);
        
        String[] actionsEnvisagees = new String[10];
        int nbActions = 0;
        
        String victoire = verifierConditionsVictoire();
        if (victoire != null) actionsEnvisagees[nbActions++] = victoire;
        
        if (nbTour - dernierTourAcheter > 40) {
            actionsEnvisagees[nbActions++] = reserverMeilleureCarte();
        }
        
        String recupLieux = construireActionLieux();
        
        if (nbPtVictoire >= 12 || (nbPtVictoire >= 10 && nbAvengers >= 2)) {
            String actionNiv3 = tenterAchatNiveau(14, 17, 2, recupLieux);
            if (actionNiv3 != null) actionsEnvisagees[nbActions++] = actionNiv3;
            
            actionsEnvisagees[nbActions++] = prendreJetonsOptimaux(InteretCouleur3(), recupLieux);
        }
        
        if (nbPtVictoire2 >= 12) {
            String blocage = bloquerAdversaire();
            if (blocage != null) actionsEnvisagees[nbActions++] = blocage;
        }
        
        String actionNiv3 = tenterAchatNiveau(14, 17, 2, recupLieux);
        if (actionNiv3 != null) actionsEnvisagees[nbActions++] = actionNiv3;
        
        actionNiv3 = tenterAchatNiveau(9, 12, 1, recupLieux);
        if (actionNiv3 != null) actionsEnvisagees[nbActions++] = actionNiv3;
        
        actionNiv3 = tenterAchatNiveau(4, 7, 0, recupLieux);
        if (actionNiv3 != null) actionsEnvisagees[nbActions++] = actionNiv3;
        
        actionsEnvisagees[nbActions++] = gererJetonsSécurisé(recupLieux);
        actionsEnvisagees[nbActions++] = actionDeSecours();
        
        for (int i = 0; i < nbActions; i++) {
            if (actionsEnvisagees[i] != null && validerAction(actionsEnvisagees[i])) {
                return actionsEnvisagees[i];
            }
        }
        
        return actionDeSecours();
    }
    
    private String actionDeSecours() {
    String recupLieux = construireActionLieux();
    
    // 1. Achat de cartes si points < 6
    if (nbPtVictoire < 6) {
        // Essayer niveau 1 d'abord
        String actionNiv1 = tenterAchatNiveauSecours(4, 7, 0, recupLieux);
        if (actionNiv1 != null) return actionNiv1;
        
        // Puis niveau 2
        String actionNiv2 = tenterAchatNiveauSecours(9, 12, 1, recupLieux);
        if (actionNiv2 != null) return actionNiv2;
        
        // Enfin niveau 3
        String actionNiv3 = tenterAchatNiveauSecours(14, 17, 2, recupLieux);
        if (actionNiv3 != null) return actionNiv3;
    }
    
    // 2. Si points < 12, priorité niveau 1
    else if (nbPtVictoire < 12) {
        String actionNiv1 = tenterAchatNiveauSecours(4, 7, 0, recupLieux);
        if (actionNiv1 != null) return actionNiv1;
        
        String actionNiv2 = tenterAchatNiveauSecours(9, 12, 1, recupLieux);
        if (actionNiv2 != null) return actionNiv2;
    }
    
    // 3. Si points >= 13, viser niveau 3
    else if (nbPtVictoire >= 13) {
        String actionNiv3 = tenterAchatNiveauSecours(14, 17, 2, recupLieux);
        if (actionNiv3 != null) return actionNiv3;
    }
    
    // 4. Prendre des jetons en priorité pour cartes niveau 3
    if (nbPtVictoire >= 13) {
        String jetonsNiv3 = prendreJetonsOptimaux(InteretCouleur3(), recupLieux);
        if (!jetonsNiv3.contains("REMETTRE 0")) return jetonsNiv3;
    }
    
    // 5. Prendre jetons optimaux généraux
    String jetonsOptimaux = prendreJetonsOptimaux(InteretCouleur(), recupLieux);
    if (!jetonsOptimaux.contains("REMETTRE 0")) return jetonsOptimaux;
    
    // 6. Si on a 9+ jetons, réserver une carte (si on n'a pas déjà 3)
    if (nbJetons >= 9 && nbCartesReservees < 3) {
        String reservation = reserverCarteSecours();
        if (reservation != null) return reservation;
    }
    
    // 7. Dernier recours : gérer les jetons de façon sécurisée
    return gererJetonsSécurisé(recupLieux);
}

private String tenterAchatNiveauSecours(int debut, int fin, int niveau, String recupLieux) {
    String[] lignes = etatDuJeu.split("\\R");
    
    // Chercher la première carte achetable
    for (int car = debut; car <= fin; car++) {
        if (carteVide(car)) continue;
        
        String[] valCarte = lignes[car].split(" ");
        if (carteAchetable(valCarte)) {
            return "JOUER " + niveau + " " + (car - debut) + recupLieux;
        }
    }
    
    return null;
}

private String reserverCarteSecours() {
    String[] lignes = etatDuJeu.split("\\R");
    
    // Priorité aux cartes de niveau 3 si points >= 10
    if (nbPtVictoire >= 10) {
        for (int car = 14; car <= 17; car++) {
            if (!carteVide(car)) {
                return "RESERVER 2 " + (car - 14);
            }
        }
    }
    
    // Sinon cartes niveau 2
    for (int car = 9; car <= 12; car++) {
        if (!carteVide(car)) {
            return "RESERVER 1 " + (car - 9);
        }
    }
    
    // Enfin cartes niveau 1
    for (int car = 4; car <= 7; car++) {
        if (!carteVide(car)) {
            return "RESERVER 0 " + (car - 4);
        }
    }
    
    return null;
}
    private boolean validerAction(String action) {
        if (action == null || action.trim().isEmpty()) return false;
        
        String[] parties = action.split(";");
        for (String partie : parties) {
            partie = partie.trim();
            if (!validerPartieAction(partie)) return false;
        }
        return true;
    }
    
    private boolean validerPartieAction(String partie) {
        if (partie.isEmpty()) return true;
        
        String[] mots = partie.split(" ");
        String commande = mots[0];
        
        switch (commande) {
            case "JETONS":
                return validerJetons(mots);
            case "REMETTRE":
                return validerRemettre(mots);
            case "RESERVER":
                return validerReserver(mots);
            case "JOUER":
                return validerJouer(mots);
            case "LIEU":
                return validerLieu(mots);
            default:
                return false;
        }
    }
    
    private boolean validerJetons(String[] mots) {
        if (mots.length < 2 || mots.length > 4) return false;
        
        int[] jetonsVoulus = new int[5];
        int totalJetons = 0;
        
        for (int i = 1; i < mots.length; i++) {
            try {
                int couleur = Integer.parseInt(mots[i]);
                if (couleur < 0 || couleur > 4) return false;
                jetonsVoulus[couleur]++;
                totalJetons++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        if (totalJetons > 3) return false;
        
        for (int c = 0; c < 5; c++) {
            if (jetonsVoulus[c] == 2 && !jetonDisponibleQuantite(c, 4)) return false;
            if (jetonsVoulus[c] > 2) return false;
            if (jetonsVoulus[c] == 1 && !jetonDisponible(c)) return false;
        }
        
        if (totalJetons == 3) {
            for (int c = 0; c < 5; c++) {
                if (jetonsVoulus[c] > 1) return false;
            }
        }
        
        return nbJetons + totalJetons <= 10;
    }
    
    private boolean validerRemettre(String[] mots) {
        if (mots.length < 2) return false;
        
        for (int i = 1; i < mots.length; i++) {
            try {
                int couleur = Integer.parseInt(mots[i]);
                if (couleur < 0 || couleur > 6) return false;
                if (jetonsCourants[couleur] <= 0) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
    
    private boolean validerReserver(String[] mots) {
        if (mots.length != 3) return false;
        if (nbCartesReservees >= 3) return false;
        
        try {
            int niveau = Integer.parseInt(mots[1]);
            int position = Integer.parseInt(mots[2]);
            
            if (niveau < 0 || niveau > 2) return false;
            if (position < 0 || position > 4) return false;
            
            if (position == 4) return true;
            
            int ligneDebut = (niveau == 0) ? 4 : (niveau == 1) ? 9 : 14;
            return !carteVide(ligneDebut + position);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean validerJouer(String[] mots) {
        if (mots.length < 2 || mots.length > 3) return false;
        
        try {
            if (mots.length == 2) {
                int carteMain = Integer.parseInt(mots[1]);
                return carteMain >= 0 && carteMain < nbCartesReservees;
            } else {
                int niveau = Integer.parseInt(mots[1]);
                int position = Integer.parseInt(mots[2]);
                
                if (niveau < 0 || niveau > 2) return false;
                if (position < 0 || position > 3) return false;
                
                int ligneDebut = (niveau == 0) ? 4 : (niveau == 1) ? 9 : 14;
                int ligneCarte = ligneDebut + position;
                
                if (carteVide(ligneCarte)) return false;
                
                String[] lignes = etatDuJeu.split("\\R");
                String[] valCarte = lignes[ligneCarte].split(" ");
                return carteAchetable(valCarte);
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean validerLieu(String[] mots) {
        if (mots.length != 2) return false;
        
        try {
            int lieuId = Integer.parseInt(mots[1]);
            String[] lignes = etatDuJeu.split("\\R");
            
            for (int i = 18; i < lignes.length; i++) {
                String[] valLieu = lignes[i].split(" ");
                if (valLieu.length > 0 && Integer.parseInt(valLieu[0]) == lieuId) {
                    if (Integer.parseInt(valLieu[6]) != -1) return false;
                    
                    for (int c = 0; c < 5; c++) {
                        if (Integer.parseInt(valLieu[c + 1]) > reducCourante[c]) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean jetonDisponibleQuantite(int couleur, int quantite) {
        if (couleur < 0 || couleur >= 5) return false;
        String[] lignes = etatDuJeu.split("\\R");
        String[] val20 = lignes[20].split(" ");
        return Integer.parseInt(val20[couleur]) >= quantite;
    }
    
    private String verifierConditionsVictoire() {
        if (nbPtVictoire >= 15) {
            if (nbAvengers < 3) {
                String actionNiv3 = chercherCarteAvengers();
                if (actionNiv3 != null) return actionNiv3;
            }
        }
        
        if (nbPtVictoire >= 14 && nbAvengers >= 2) {
            String actionOptimale = chercherVictoireDirecte();
            if (actionOptimale != null) return actionOptimale;
        }
        
        return null;
    }
    
    private String chercherCarteAvengers() {
        String[] lignes = etatDuJeu.split("\\R");
        int meilleurScore = -1;
        String meilleureAction = null;
        
        for (int car = 14; car <= 17; car++) {
            if (carteVide(car)) continue;
            String[] valCarte = lignes[car].split(" ");
            
            if (carteAchetable(valCarte)) {
                int avengers = Integer.parseInt(valCarte[9]);
                int points = Integer.parseInt(valCarte[8]);
                int score = avengers * 10 + points;
                
                if (score > meilleurScore) {
                    meilleurScore = score;
                    meilleureAction = "JOUER 2 " + (car - 14) + construireActionLieux();
                }
            }
        }
        
        return meilleureAction;
    }
    
    private String chercherVictoireDirecte() {
        String[] lignes = etatDuJeu.split("\\R");
        
        for (int car = 14; car <= 17; car++) {
            if (carteVide(car)) continue;
            String[] valCarte = lignes[car].split(" ");
            
            if (carteAchetable(valCarte)) {
                int points = Integer.parseInt(valCarte[8]);
                int avengers = Integer.parseInt(valCarte[9]);
                
                if (nbPtVictoire + points >= 16 || 
                    (nbAvengers + avengers >= 3 && nbPtVictoire + points >= 14)) {
                    return "JOUER 2 " + (car - 14) + construireActionLieux();
                }
            }
        }
        
        for (int car = 9; car <= 12; car++) {
            if (carteVide(car)) continue;
            String[] valCarte = lignes[car].split(" ");
            
            if (carteAchetable(valCarte)) {
                int points = Integer.parseInt(valCarte[8]);
                if (nbPtVictoire + points >= 16) {
                    return "JOUER 1 " + (car - 9) + construireActionLieux();
                }
            }
        }
        
        return null;
    }
    
    private void parseEtatJeu(String etat) {
        String[] lignes = etat.split("\\R");
        
        String[] val1 = lignes[1].split(" ");
        for (int i = 0; i < 5; i++) {
            reducCourante[i] = Integer.parseInt(val1[i + 1]);
            jetonsCourants[i] = Integer.parseInt(val1[i + 6]);
        }
        jetonsCourants[5] = Integer.parseInt(val1[11]);
        jetonsCourants[6] = Integer.parseInt(val1[12]);
        nbJetons = sommeTableau(jetonsCourants, 7);
        nbCartesReservees = Integer.parseInt(val1[13]);
        nbPtVictoire = Integer.parseInt(val1[14]);
        nbAvengers = Integer.parseInt(val1[15]);
        rassemblementAvengers = Integer.parseInt(val1[16]) == 1;
        
        String[] val2 = lignes[2].split(" ");
        for (int i = 0; i < 5; i++) {
            reducSuivante[i] = Integer.parseInt(val2[i + 1]);
            jetonsSuivants[i] = Integer.parseInt(val2[i + 6]);
        }
        jetonsSuivants[5] = Integer.parseInt(val2[11]);
        jetonsSuivants[6] = Integer.parseInt(val2[12]);
        nbPtVictoire2 = Integer.parseInt(val2[14]);
    }
    
    private String gererJetonsSécurisé(String recupLieux) {
        if (nbJetons > 10) {
            return "REMETTRE " + choisirJetonAJeter() + recupLieux;
        }
        
        int[] interets = InteretCouleur();
        int[] classement = Max5(interets);
        
        List<String> prenables = new ArrayList<>();
        List<String> jetables = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            if (jetonDisponible(classement[i])) {
                prenables.add(String.valueOf(classement[i]));
            }
            if (jetonEnMain(classement[4-i])) {
                jetables.add(String.valueOf(classement[4-i]));
            }
        }
        
        int maxPrenables = Math.min(3, prenables.size());
        int jetonsApres = nbJetons + maxPrenables;
        
        if (jetonsApres <= 10) {
            return construireActionJetons(prenables, recupLieux, maxPrenables);
        } else {
            int aJeter = jetonsApres - 10;
            List<String> aJeterListe = jetables.subList(0, Math.min(aJeter, jetables.size()));
            
            String prendre = construireChaine(prenables, maxPrenables);
            String jeter = construireChaine(aJeterListe, aJeter);
            
            return "JETONS " + prendre + "; REMETTRE " + jeter + recupLieux;
        }
    }
    
    private String choisirJetonAJeter() {
        int[] interets = InteretCouleur();
        int minInteret = Integer.MAX_VALUE;
        int couleurAJeter = -1;
        
        for (int c = 0; c < 5; c++) {
            if (jetonsCourants[c] > 0 && interets[c] < minInteret) {
                minInteret = interets[c];
                couleurAJeter = c;
            }
        }
        
        return couleurAJeter != -1 ? String.valueOf(couleurAJeter) + " " : "0 ";
    }
    
    private String reserverMeilleureCarte() {
        if (nbCartesReservees >= 3) {
            return gererJetonsSécurisé("");
        }
        
        int maxInteret = -10, carteMax = -1;
        
        if (nbPtVictoire >= 10) {
            for (int car = 14; car <= 17; car++) {
                if (!carteVide(car)) {
                    int interet = interetCarte(car);
                    if (interet > maxInteret) {
                        maxInteret = interet;
                        carteMax = car;
                    }
                }
            }
            if (carteMax != -1) {
                dernierTourAcheter = nbTour;
                return "RESERVER 2 " + (carteMax - 14);
            }
        }
        
        for (int car = 4; car <= 7; car++) {
            if (!carteVide(car)) {
                int interet = interetCarte(car);
                if (interet > maxInteret) {
                    maxInteret = interet;
                    carteMax = car;
                }
            }
        }
        
        if (carteMax != -1) {
            dernierTourAcheter = nbTour;
            return "RESERVER 0 " + (carteMax - 4);
        }
        
        return gererJetonsSécurisé("");
    }
    
    private String construireActionLieux() {
        String lieu0 = lieuRecuperable(0) ? "; LIEU " + getLieuId(18) : "";
        String lieu1 = (lieuRecuperable(1) && !lieuRecuperable(0)) ? "; LIEU " + getLieuId(19) : "";
        return lieu0 + lieu1;
    }
    
    private String tenterAchatNiveau(int debut, int fin, int niveau, String recupLieux) {
        String[] lignes = etatDuJeu.split("\\R");
        
        String meilleureAction = null;
        int meilleurScore = -1;
        
        for (int car = debut; car <= fin; car++) {
            if (carteVide(car)) continue;
            
            String[] valCarte = lignes[car].split(" ");
            if (!carteAchetable(valCarte)) continue;
            
            int points = Integer.parseInt(valCarte[8]);
            int avengers = Integer.parseInt(valCarte[9]);
            
            int score = points * 2 + avengers * 3;
            if (nbPtVictoire >= 13) score += points * 5;
            if (nbAvengers >= 2) score += avengers * 10;
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureAction = "JOUER " + niveau + " " + (car - debut) + recupLieux;
            }
        }
        
        if (meilleureAction != null) {
            dernierTourAcheter = nbTour;
        }
        
        return meilleureAction;
    }
    
    private String prendreJetonsOptimaux(int[] interets, String recupLieux) {
        int[] classement = Max5(interets);
        List<String> prenables = new ArrayList<>();
        
        for (int i = 0; i < Math.min(3, classement.length); i++) {
            if (jetonDisponible(classement[i])) {
                prenables.add(String.valueOf(classement[i]));
            }
        }
        
        return construireActionJetons(prenables, recupLieux, Math.min(3, prenables.size()));
    }
    
    private String bloquerAdversaire() {
        String[] lignes = etatDuJeu.split("\\R");
        
        if (nbCartesReservees >= 3) return null;
        
        for (int car = 14; car <= 17; car++) {
            if (carteVide(car)) continue;
            
            String[] valCarte = lignes[car].split(" ");
            if (carteAchetableAdv(valCarte)) {
                int points = Integer.parseInt(valCarte[8]);
                if (nbPtVictoire2 + points >= 16) {
                    return "RESERVER 2 " + (car - 14);
                }
            }
        }
        return null;
    }
    
    private String construireActionJetons(List<String> jetons, String recupLieux, int limite) {
        if (jetons.isEmpty()) return "REMETTRE 0" + recupLieux;
        
        String jetonsStr = construireChaine(jetons, limite);
        return "JETONS " + jetonsStr + recupLieux;
    }
    
    private String construireChaine(List<String> liste, int limite) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(limite, liste.size()); i++) {
            sb.append(liste.get(i)).append(" ");
        }
        return sb.toString().trim() + " ";
    }
    
    private boolean carteAchetable(String[] valeurs) {
        int[] couts = new int[5];
        for (int i = 0; i < 5; i++) {
            couts[i] = Integer.parseInt(valeurs[i + 3]);
        }
        
        int jetonsManquants = 0;
        for (int i = 0; i < 5; i++) {
            int manque = Math.max(0, couts[i] - reducCourante[i] - jetonsCourants[i]);
            jetonsManquants += manque;
        }
        
        return jetonsManquants <= jetonsCourants[5];
    }
    
    private boolean carteAchetableAdv(String[] valeurs) {
        for (int i = 0; i < 5; i++) {
            int cout = Integer.parseInt(valeurs[i + 3]);
            if (cout > reducSuivante[i] + jetonsSuivants[i] + jetonsSuivants[5]) {
                return false;
            }
        }
        return true;
    }
    
    private boolean carteEnvisageable(String[] valeurs) {
        for (int i = 0; i < 5; i++) {
            int cout = Integer.parseInt(valeurs[i + 3]);
            if (cout - reducCourante[i] > 6) return false;
        }
        return true;
    }
    
    private boolean jetonDisponible(int j) {
        if (j < 0 || j >= 5) return false;
        String[] lignes = etatDuJeu.split("\\R");
        String[] val20 = lignes[20].split(" ");
        return Integer.parseInt(val20[j]) > 0;
    }
    
    private boolean jetonEnMain(int j) {
        return j >= 0 && j < 5 && jetonsCourants[j] > 0;
    }
    
    private boolean carteVide(int pos) {
        String[] lignes = etatDuJeu.split("\\R");
        String[] valPos = lignes[pos].split(" ");
        return Integer.parseInt(valPos[2]) == -1;
    }
    
    private boolean lieuRecuperable(int posLieu) {
        String[] lignes = etatDuJeu.split("\\R");
        String[] valLieu = lignes[18 + posLieu].split(" ");
        
        if (Integer.parseInt(valLieu[6]) != -1) return false;
        
        for (int i = 0; i < 5; i++) {
            if (Integer.parseInt(valLieu[i + 1]) > reducCourante[i]) {
                return false;
            }
        }
        return true;
    }
    
    private int getLieuId(int ligne) {
        String[] lignes = etatDuJeu.split("\\R");
        String[] val = lignes[ligne].split(" ");
        return Integer.parseInt(val[0]);
    }
    
    private int[] InteretCouleur() {
        return calculerInteret(4, 7);
    }
    
    private int[] InteretCouleur3() {
        return calculerInteret(14, 17);
    }
    
    private int[] calculerInteret(int debut, int fin) {
        int[] interet = new int[5];
        String[] lignes = etatDuJeu.split("\\R");
        
        for (int c = 0; c < 5; c++) {
            for (int car = debut; car <= fin; car++) {
                if (carteVide(car)) continue;
                
                String[] valCarte = lignes[car].split(" ");
                if (!carteEnvisageable(valCarte)) continue;
                
                int cout = Integer.parseInt(valCarte[3 + c]);
                int manque = Math.max(0, cout - reducCourante[c] - jetonsCourants[c]);
                
                if (manque > 0 && cout > 0) {
                    int poids = 1;
                    if (nbPtVictoire >= 13) poids = 3;
                    else if (nbPtVictoire >= 10) poids = 2;
                    
                    if (debut == 14) {
                        interet[c] += poids * 2;
                    } else {
                        interet[c] += (manque == 1) ? 3 * poids : (manque == 2) ? 2 * poids : poids;
                    }
                }
            }
        }
        return interet;
    }
    
    private int[] Max5(int[] tableau) {
        int[] indices = new int[5];
        boolean[] utilises = new boolean[tableau.length];
        
        for (int i = 0; i < 5; i++) {
            int maxVal = -1;
            int maxIdx = 0;
            
            for (int j = 0; j < tableau.length; j++) {
                if (!utilises[j] && tableau[j] > maxVal) {
                    maxVal = tableau[j];
                    maxIdx = j;
                }
            }
            
            indices[i] = maxIdx;
            utilises[maxIdx] = true;
        }
        
        return indices;
    }
    
    private int interetCarte(int pos) {
        String[] lignes = etatDuJeu.split("\\R");
        String[] valCarte = lignes[pos].split(" ");
        
        if (!carteEnvisageable(valCarte)) return -20;
        
        int res = 0;
        int totalManque = 0;
        
        for (int c = 0; c < 5; c++) {
            int cout = Integer.parseInt(valCarte[3 + c]);
            int manque = Math.max(0, cout - reducCourante[c] - jetonsCourants[c]);
            totalManque += manque;
            
            if (manque == 0 && cout > 0) res += 10;
            else if (manque == 1) res += 8;
            else if (manque == 2) res += 5;
            else if (manque == 3) res += 2;
            else if (manque > 3) res -= 5;
        }
        
        if (totalManque > jetonsCourants[5] + 2) res -= 15;
        
        int coulCarte = Integer.parseInt(valCarte[1]);
        if (lieuRecuperable(0)) res += CouleurLieu1()[coulCarte];
        if (lieuRecuperable(1)) res += CouleurLieu2()[coulCarte];
        
        int points = Integer.parseInt(valCarte[8]);
        int avengers = Integer.parseInt(valCarte[9]);
        
        res += points * 3;
        res += avengers * 5;
        
        if (nbPtVictoire >= 12) {
            res += points * 10;
            res += avengers * 15;
        }
        
        return res;
    }
    
    private int[] CouleurLieu1() {
        return getCouleurLieu(18);
    }
    
    private int[] CouleurLieu2() {
        return getCouleurLieu(19);
    }
    
    private int[] getCouleurLieu(int ligne) {
        if (etatDuJeu == null) return new int[5];
        
        String[] lignes = etatDuJeu.split("\\R");
        if (ligne >= lignes.length) return new int[5];
        
        String[] val = lignes[ligne].split(" ");
        if (val.length < 6) return new int[5];
        
        int[] couleurs = new int[5];
        for (int i = 0; i < 5; i++) {
            couleurs[i] = Integer.parseInt(val[i + 1]);
        }
        return couleurs;
    }
    
    private int sommeTableau(int[] tableau, int limite) {
        int somme = 0;
        for (int i = 0; i < Math.min(limite, tableau.length); i++) {
            somme += tableau[i];
        }
        return somme;
    }
    
    @Override
    public void init(String etatDuJeu) {
        nbTour = 0;
        dernierTourAcheter = 0;
        
        reducCourante = new int[5];
        jetonsCourants = new int[7];
        reducSuivante = new int[5];
        jetonsSuivants = new int[7];
        
        parseEtatJeu(etatDuJeu);
    }
}