# othelloecn
Github option info 2017 projet de groupe Othello


## Support Multi-plateformes

### Windows

Il suffit de compiler le projet avec ANT (voir paragraphe suivant) ou Netbeans et de placer la bibliothèque ```fannfloat.dll``` dans le même dossier que ```Omegathello.jar```.
Alternativement, il est possible d'utiliser directement les binaires situés dans le dossier ```bin```.

(Testé sous Windows 10)

### Linux

Il faudra pour pouvoir utiliser les réseaux de neurones compiler et installer la bibliothèque Fann en suivant les instructions décrites sur la page GitHub du projet : https://github.com/libfann/fann/ .

**Note** : le réseau de neurones ```ann_128``` fourni par défaut ayant été créé sous Windows avec une version plus ancienne de la bibliothèque (FANN 2.1), il ne fonctionnera pas sous Linux avec la dernière version de FANN (v2.2). Seul les réseaux créés avec l'interface ```ann``` pourront être utilisés.

(Partiellement testé sous machine virtuelle Ubuntu 16.04.1)



## Compilation avec ANT

Le projet peut facilement être compilé avec ANT en exécutant la commande ```ant``` à la racine du projet.

Les tâches effectués par ANT sont les suivantes :
 
- Compilation du projet 
- Compilation et exécution des tests unitaires avec JUnit
- Compilation de la Javadoc
- Calcul de la couverture de code avec Jacoco
- Evaluation de la qualité du code avec SonarQube

La bonne exécution du dernier point nécessite d'avoir SonarQube installé et lancé.



## Support de Netbeans

Il est possible d'ouvrir et de compiler/exécuter le projet avec Netbeans en effectuant les étapes suivantes :

1. Effectuer un ```git clone``` du projet
2. Dans Netbeans, créer un nouveau projet Java à partir de fichiers existants :
    * en sélectionnant pour dossier du projet le dossier créé par ```git clone```,
	* en choisissant comme nom de fichier de build un nom différent de ```build.xml``` (par ex. ```nbbuild.xml```),
    * puis ajoutant le dossier ```src``` (resp. ```test```) comme dossier de package des sources (resp. des tests)
3. Si Netbeans vous indique qu'il y a des problèmes non résolus, suivez simplement la procédure permettant de les résoudre

Vous devriez ensuite être capable de travailler sur le projet depuis Netbeans.


## Interface en ligne de commande 

Le programme propose une interface en ligne de commande permettant de jouer.

Plusieurs options sont disponibles pour modifier le comportement du jeu.

- ```m```
  * Syntaxe : ```-m <mode>```
  * Description : le mode de jeu
  * Valeur possible pour ```<mode>``` :
     * ```HvM``` (default) : l'homme affronte la machine
	 * ```HvH``` : l'homme affronte l'homme
	 * ```MvM``` : la machine affronte la machine
- ```ai```
  * Syntaxe : ```-ai <nom-ia>```
  * Description : l'IA a utiliser
  * Valeur possible pour ```<nom-ia>``` : voir la section IA implémentées
  * Note : cette option peut être utilisée deux fois dans le cas d'un mode MvM pour spécifier les deux IA a utiliser
- ```-ai-opts```
  * Syntaxe : ```--ai-opts <options>```
  * Description : les options de construction pour l'IA
  * Valeur possible pour ```<options>``` : dépend de chaque IA
  * Note : cette option peut être utilisée deux fois dans le cas d'un mode MvM pour spécifier les deux IA a utiliser

  
### Exemple 

Démarrer le jeu en mode 1 contre 1.

```bash
java -jar Omegathello.jar -m HvH
```

Démarrer le jeu avec une ia utilisant l'algorithme minmax à une profondeur de 4.

```bash
java -jar Omegathello.jar -ai minmax --ai-opts depth=4
```


Démarrer le jeu en mode machine vs. machine avec un MinMax contre un MonteCarlo.

```bash
java -jar Omegathello.jar -m MvM -ai minmax --ai-opts depth=5 -ai montecarlo --ai-opts num=100,time=500
```



## Création et ajout d'une IA

La création d'une IA se fait en dérivant de la classe AbstractAI du package ai. 
Il suffit de redéfinir les méthodes suivantes de la classe abstraite :

- notifyPass() : utilisée par l'interface pour indiquer à l'IA qu'elle va passer son tour ;
- notifyRewind() : utilisée par l'UI pour indiquer à l'IA que le jeu effectue un retour en arrière ;
- notifyMove() : utilisée par l'UI pour indiquer qu'un coup vient d'être joué (utiliser aussi bien pour l'IA elle-même que pour son adversaire) ;
- notifyLoad() : utilisée par l'UI pour indiquer à l'IA qu'une partie vient d'être chargée ;
- selectMoveWithTimeout() : demande à l'IA de sélectionner un coup parmi ceux jouable dans une limite de temps donnée.

Pour informer l'interface de l'existance de l'IA, il suffit : 

- de lui donner un nom (sans espace) permettant de l'identifier de manière unique en définnisant un attribut ```static``` et ```public``` de type ```String``` ayant pour nom ```NAME``` ;

```java
public static String NAME = "myai";
```

- d'ajouter dans la fonction ```availableAIs()``` du fichier Main.java la ligne correspondant à votre IA.

```java
ais.add(MyClassAI.class);
```


## IA implémentées

Plusieurs intelligences artificielles sont fournies par défaut : 

- ```minmax``` : une IA utilisant l'algorithme minmax avec un élagage alpha-beta ;
- ```montecarlo``` : une IA utilisant la recherche de MonteCarlo pour les arbres.


### IA MinMax

Cette IA utilise l'algorithme minmax pour décider quel coup jouer.
L'IA utilise une fonction d'évaluation pour évaluer la situation en milieux de partie. 
Elle explore l'arbre à une profondeur fixe, donnée en paramètre au constructeur avec l'option ```depth```, 
modulo un éventuel élagage.

#### Options de construction

- ```depth```
  * Syntaxe : ```depth=3```
  * Description : contrôle la profondeur d'exploration de l'arbre
- ```func```
  * Syntaxe : ```func=ssymef```
  * Description : indique la fonction d'évaluation utilisée pour évaluer le plateau
  * Fonctions disponibles :
     * ```ssymef``` (default) - une fonction d'évaluation symétrique très simple
	 
Note : les options sont séparés par des virgules.
	 
### IA MonteCarlo

Cette IA utilise la recherche d'arbre de MonteCarlo pour décider quel coup choisir.
L'IA explore "au hasard" les branches de l'arbre en jouant les parties jusqu'au bout, elle 
est alors en mesure de savoir quelle branche présente à priori la plus forte probabilité de victoire.
Le nombre de branches explorées à chaque étape peut être fixée grâce à l'option ```num```. Alternativement, 
un temps maximum d'exploration peut être définie avec l'option ```time```.


#### Options de construction

- ```num```
  * Syntaxe : ```num=64```
  * Description : Le nombre de branche à explorer à chaque étape (défaut:64).
  * Note : la valeur -1 peut être utilisée pour indiquer que seule la limite de temps est à prendre en compte
- ```time```
  * Syntaxe : ```time=750```
  * Description : indique le temps maximal (en millisecondes) autorisé pour l'exploration des branches (défaut:-1)
  * Note : la valeur -1 peut être utilisée pour indiquer que seule la limite en nombre de branche est à prendre en compte.
  
Note : les options ```num``` et ```time``` peuvent être combinés ; les options sont séparés par des virgules.


### IA NeuralNetwork

Cette IA utilise l'algorithme minmax pour décider quel coup jouer, mais utilise un réseau de neurones comme fonction d'évaluation.


#### Options de construction

- ```depth```
  * Syntaxe : ```depth=3```
  * Description : contrôle la profondeur d'exploration de l'arbre
- ```ann```
  * Syntaxe : ```ann=myann```
  * Description : indique quel réseau de neurones utiliser pour évaluer le plateau
  * Réseaux disponibles :
     * ```ann_128``` (default) - un réseau de neurones ayant une couche interne de cardinal 128, entraîner avec un jeu d'entraînement contenant 1000 entrées et ayant un peu muté
	 * tous les réseaux créés avec l'interface dédiée au réseaux de neurones.
	 
Note : les options sont séparés par des virgules.



## Interface en ligne de commande pour les réseaux de neurones

Le programme propose une interface en ligne de commande permettant de créer/modifier des réseaux de neurones. 
Les réseaux de neurones sont utilisés comme fonction d'évalutation pour l'IA ```ann``` dont le fonctionnement est décrit plus haut.

L'interface est accessible en fournissant ```ann``` comme premier argument du programme Java.

Plusieurs commandes sont alors disponibles : ```create```, ```copy```, ```destroy```, ```generate```, ```train```, ```test``` et ```mutate```.

- ```create```
  * Syntaxe : ```create [--layers <l>] <name>```
  * Description : créer un réseau de neurones avec le nom donné en paramètre
  * Option ```--layers``` : permet de configurer l'architecture du réseau, ```<l>``` est une liste d'entiers séparés par ```:``` indiquant le nombre de neurones dans chaque couche.
  * Note : par défaut, le réseau est créer avec une couche interne contenant 42 neurones
  * Note : si un réseau de neurones ayant le nom donné existe déjà, il est écrasé
- ```copy```
  * Syntaxe : ```copy <src> <dest>```
  * Description : créer une copie de l'ann ```src``` sous le nom ```dest```
- ```destroy```
  * Syntaxe : ```destroy <name>```
  * Description : supprime le réseau de neurones ayant le nom donné en paramètre
- ```generate```
  * Syntaxe : ```generate [--eval <func>] [(n|d:n)*] <name>```
  * Description : génère un set d'entraînement (couple configuration-evaluation) ayant pour nom ```name``` pour entraîner les réseaux de neurones, il est possible de préciser un nombre de configuration a générer et de demander à n'avoir que des configurations correspondants à un certain nombre de coups joués.
  * Valeurs possibles pour ```func``` (argument ```--eval``` optionnel) : 
     * ```token-count``` (default) : une fonction d'évaluation renvoyant ```[jeton(joueur)-jeton(adversaire)]/max(jeton(joueur),jeton(adversaire))```
  * Note : les configurations sont générés aléatoirements
  * Exemples :
     * ```generate 1000 ts1``` génère 1000 configurations correspondant chacune à un nombre de coups joués aléatoire
	 * ```generate 20:300 ts1``` génère 300 configurations correspondant chacune à un nombre de coups joués égal à 20
	 * ```generate 1000 20:300 ts1``` génère 1000 config aléatoires et 300 config de profondeur 20.
- ```train```
  * Syntaxe : ```train [--max-epochs <me>] [--report <r>] [--error <e>] <ann-name> <ts-name>```
  * Description : entraîne le réseaux de neurones ```<ann-name>``` avec le jeu d'entraînement ```<ts-name>```
  * Option ```--max-epochs``` (opt) :
     * Description : défini le nombre maximum d'itération d'apprentissage autorisée
	 * Valeur ```<me>``` : entier > 0 (default:1000)
  * Option ```--report``` (opt) :
     * Description : défini le nombre d'itérations séparant deux rapports de progrès dans l'apprentissage
	 * Valeur ```<r>``` : entier > 0 (default:100)
  * Option ```--error``` (opt) :
     * Description : défini l'erreur désiré du réseau de neurones pour les entrées du set d'entraînement
	 * Valeur ```<e>``` : flottant > 0 (default:0.0001f)
  * Note : cette commande utilise la fonction ```fann_train_on_file()``` de la bibliothèque ```FANN```, consulter la documentation associée pour plus de détails
- ```test```
  * Syntaxe : ```test <ann-name>```
  * Description : démarre le mode de test interactif pour le réseau de neurones donné, voir la description du mode test ci-après.
- ```mutate```
  * Syntaxe : ```mutate [--epochs <e>] [--time <t>] [--round <r>] [--ai <a>] [--ai-opts <o>] [-depth <d>] <ann-name>```
  * Description : démarre le processus de mutation pour le réseau de neurone ```ann-name```
  * Option ```--epochs``` (opt) :
     * Description : défini le nombre de cycle de mutation
	 * Valeur ```<e>``` : entier > 0 (default:1000)
  * Option ```--time``` (opt) :
     * Description : défini le temps maximal autorisé pour le processus de mutation
	 * Valeur ```<t>``` : entier > 0 (default:-1)
	 * Note : la valeur -1 indique l'absence de limite de temps
  * Option ```--round``` (opt) :
     * Description : défini le nombre de matchs par cycle de mutation
	 * Valeur ```<r>``` : entier > 0 (default:100)
  * Option ```--ai``` (opt) :
     * Description : nom de l'ia a utiliser comme adversaire de l'ia fonctionnant avec le réseau de neurones
	 * Valeur ```<a>``` (default:random) : une des ia introduites plus haut.
  * Option ```--ai-opts``` (opt) :
     * Description : les options de construction de l'ia affrontant le réseau de neurones
	 * Valeur ```<o>``` (default:"") : dépend de l'ia considérée
  * Option ```--depth``` (opt) :
     * Description : la pronfondeur utilisé pour le minmax du réseau de neurones
	 * Valeur ```<d>``` (default:3) : entier >= 0


	 
### Mode test

Ce mode interactif permet de jouer au jeu de manière similaire au mode deux joueurs, mais en donnant la possibilité d'intérroger le réseau de neurones pour évaluer la configuration.

Ce mode propose plusieurs commandes :

- ```-m``` : affiche les coups possibles
- ```-r <n>``` : retourne n coups en arrière
- ```-a <n>``` : avance n coups en avants (au hasard)
- ```-e``` : affiche la valeur calculé par le réseau de neurones pour la configuration actuelle
- ```-exit``` : quitte le programme

et permet de jouer les coups de la même manière que dans les modes de jeu.


### Mode mutate

Le mode ```mutate``` permet de démarrer un processus de mutation dans le but d'améliorer le réseau de neurones. 
Le principe est le suivante :

- on fait s'affronter le réseau de neurones avec une autre IA sur un nombre de matchs donné, la moitié en tant que joueur blanc, l'autre en temps que joueur noir ;
- on change légèrement le coefficient d'un nombre (pour l'instant) fixe (égal à 1) de connexions du réseau de neurones, et on répète le point précédent ;
- on garde le réseau de neurones ayant gagner le plus de points, avec la convention 1 victoire = 2 points et 1 égalité = 1 point.

En itérant de nombreuses fois ce processus, on peut espérer obtenir une IA plus forte qu'à l'origine.

Le mode permet de spécifier le nombre de matchs par cycle de mutation (option ```--round```), plus il est élevé, plus les résultats seront "fiables", mais cela impliquera que chaque cycle prend plus de temps.

En pratique, on n'arrivera pas à des résultats convainquants car la durée de chaque cycle est trop importante pour pouvoir effectuer assez de cycle de mutation.






### Exemple 

Créer un réseau de neurones possédant deux couches internes ayant respectivement 32 et 16 neurones

```bash
java -jar Omegathello.jar ann create --layers 32:16 myann
```

Créer un jeu d'entraînement contenant 500 configurations aléatoires, 250 configurations de profondeur 25, et 250 configurations de profondeur 35

```bash
java -jar Omegathello.jar ann generate 500 25:250 35:250 ts1
```

Entraîner le réseaux de neurones avec le jeu précédement créer

```bash
java -jar Omegathello.jar ann train myann ts1
```

Démarrer le processus de mutation, contre une IA de type MonteCarlo ayant droit à 10ms pour jouer, en utilisant un MinMax de pronfondeur 4 pour le réseau de neurones, et en limitant à 50 matchs par round.

```bash
java -jar Omegathello.jar ann mutate --round 50 --ai montecarlo --ai-opts time=10 --depth 4 myann
```

Jouer contre l'IA créée

```bash
java -jar Omegathello.jar -m HvM -ai ann --ai-opts ann=myann
```

Ou la laisser jouer contre une autre IA

```bash
java -jar Omegathello.jar -m MvM -ai ann --ai-opts ann=myann -ai montecarlo --ai-opts time=25
```