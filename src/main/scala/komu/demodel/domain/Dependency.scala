package komu.demodel.domain;

final class Dependency(to: Module, kind: DependencyType) {
  override def toString = "to=" + to + ", kind=" + kind;
}
