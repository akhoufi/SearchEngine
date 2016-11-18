# SearchEngine

Etapes du moteur de recherche : 
- Construction des fichiers inverses par paquets (par jour par exemple)
- Fusion des fichiers inverses pour construire le fichier inverse (index) de toutes les pages.
- Construction des fichiers .poids de toutes les pages (les mettre dans weights/ par exemple)

Pour une requete donnée : 
- Considérer la requete comme un document et calculer son fichier .poids
- Calculer la similarité entre la requete et tout les documents contenant les mots clées de la requete(à l'aide des .poids)
- Trier les pages par similarité decroissante
- Faire la liaison entre les noms de hashage et le nom de ces pages pour les afficher


How it works: 

* Put your data (text directory) in the same level as the project
* To search a term just change the list string "query" in the file "Main.SearchEngine"