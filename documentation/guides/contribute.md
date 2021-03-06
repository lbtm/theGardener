```thegardener
{
  "page" :
     {
        "label": "Contribut",
        "description": "How to contribut to theGardener development ?"
     }
}
```

theGardener is an open sourced project, it's there thank to the contributors. 

![Roles](../assets/images/theGardener_role_contributor.png)


## Want to give feedback

Want to 

- Give feedback,
- Raise issues,
- Propose enhancement ? 

Please create an issue on https://github.com/KelkooGroup/theGardener/issues/new

## Want to talk to a human

Join us on [Discord](https://discordapp.com/channels/417704230531366923/417704230976225281) 

## Want to develop  


### Install a dev environment

TODO....

### Development on Back


TODO....

Before push, 

```
sbt test
sbt scapegoat
```


### Development on Front

The front is under _frontend_ directory.


TODO....

Before push, 

```
ng test
ng lint --fix
```



### Developer guide lines

1. **Eat our own dog food**: We are building an application to help BDD (Behavior Driven Development), so let's drive the development by Cucumber scenarios ! And when the application will be enough evolved, just use it for our own development and documentation.
1. **Keep It Simple**: Do not bother complicate the code or anticipate future features. With a good BDD cover, we will be able to refactor our code easily if needed.
1. **Clean code**: Let's have a nice source base that we are proud of. ([Clean Code Cheat Sheet ](https://www.bbv.ch/images/bbv/pdf/downloads/V2_Clean_Code_V3.pdf))
1. **Convention over configuration**: Make life as easy as possible to theGardener users: the code should adapt to some convention so that users have the less possible configuration to define.
1. **Enjoy !**
